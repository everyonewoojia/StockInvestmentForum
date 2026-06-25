package com.stock.forum.service;

import com.stock.forum.common.ApiException;
import com.stock.forum.config.AppProperties;
import com.stock.forum.domain.OcrUploadEntity;
import com.stock.forum.dto.OcrDtos;
import com.stock.forum.external.TencentOcrClient;
import com.stock.forum.repository.OcrUploadRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class OcrService {
    private static final DateTimeFormatter DIR_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_URL_PREFIX = "/api/medicineBox/ocr/file/";

    private final AppProperties properties;
    private final OcrUploadRepository uploadRepository;
    private final TencentOcrClient tencentOcrClient;
    private final OcrParser ocrParser;

    public OcrService(AppProperties properties, OcrUploadRepository uploadRepository, TencentOcrClient tencentOcrClient, OcrParser ocrParser) {
        this.properties = properties;
        this.uploadRepository = uploadRepository;
        this.tencentOcrClient = tencentOcrClient;
        this.ocrParser = ocrParser;
    }

    @Transactional
    public OcrDtos.UploadResponse upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("file is required");
        }
        try {
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            String extension = extensionOf(originalName);
            String dateDir = LocalDate.now().format(DIR_DATE);
            String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path baseDir = Paths.get(properties.getUpload().getDir()).toAbsolutePath().normalize();
            Path targetDir = baseDir.resolve(dateDir).normalize();
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(storedName).normalize();
            if (!target.startsWith(baseDir)) {
                throw ApiException.badRequest("Invalid upload path");
            }
            Files.copy(file.getInputStream(), target);

            OcrUploadEntity upload = new OcrUploadEntity();
            upload.setUserId(userId);
            upload.setOriginalName(originalName);
            upload.setStoredName(storedName);
            upload.setRelativePath(dateDir + "/" + storedName);
            upload.setContentType(file.getContentType());
            upload.setImageUrl(FILE_URL_PREFIX + storedName);
            uploadRepository.save(upload);

            OcrDtos.UploadResponse response = new OcrDtos.UploadResponse();
            response.imageUrl = upload.getImageUrl();
            return response;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.serverError("Failed to save upload");
        }
    }

    @Transactional(readOnly = true)
    public FileData loadFile(String storedName) {
        OcrUploadEntity upload = uploadRepository.findByStoredName(storedName)
                .orElseThrow(() -> ApiException.badRequest("File not found"));
        Path path = Paths.get(properties.getUpload().getDir()).toAbsolutePath().normalize()
                .resolve(upload.getRelativePath()).normalize();
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw ApiException.badRequest("File not found");
        }
        return new FileData(resource, upload.getContentType());
    }

    @Transactional(readOnly = true)
    public OcrDtos.MedicineInfoResponse recognize(String userIdValue, String imageUrl) {
        Long userId = AuthGuard.requireSelf(userIdValue);
        if (!StringUtils.hasText(imageUrl)) {
            throw ApiException.badRequest("imageUrl is required");
        }
        List<String> lines;
        String storedName = extractStoredName(imageUrl);
        if (storedName != null) {
            OcrUploadEntity upload = uploadRepository.findByStoredNameAndUserId(storedName, userId)
                    .orElseThrow(() -> ApiException.badRequest("Uploaded image not found"));
            lines = tencentOcrClient.recognizeByBase64(toBase64(upload));
        } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            lines = tencentOcrClient.recognizeByUrl(imageUrl);
        } else {
            throw ApiException.badRequest("imageUrl must be an uploaded file URL or public URL");
        }
        return ocrParser.parse(lines);
    }

    private String toBase64(OcrUploadEntity upload) {
        try {
            Path path = Paths.get(properties.getUpload().getDir()).toAbsolutePath().normalize()
                    .resolve(upload.getRelativePath()).normalize();
            return Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        } catch (Exception ex) {
            throw ApiException.serverError("Failed to read uploaded image");
        }
    }

    private String extractStoredName(String imageUrl) {
        int index = imageUrl.indexOf(FILE_URL_PREFIX);
        if (index < 0) {
            return null;
        }
        String value = imageUrl.substring(index + FILE_URL_PREFIX.length());
        int queryIndex = value.indexOf('?');
        if (queryIndex >= 0) {
            value = value.substring(0, queryIndex);
        }
        return value.trim().isEmpty() ? null : value;
    }

    private String extensionOf(String originalName) {
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return ".jpg";
        }
        String extension = originalName.substring(index).toLowerCase();
        if (extension.length() > 10) {
            return ".jpg";
        }
        return extension;
    }

    public static class FileData {
        public final Resource resource;
        public final String contentType;

        public FileData(Resource resource, String contentType) {
            this.resource = resource;
            this.contentType = contentType;
        }
    }
}
