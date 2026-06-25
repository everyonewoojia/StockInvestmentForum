package com.stock.forum.controller;

import com.stock.forum.auth.AuthContext;
import com.stock.forum.common.ApiResponse;
import com.stock.forum.dto.OcrDtos;
import com.stock.forum.service.MedicineService;
import com.stock.forum.service.OcrService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/medicineBox/ocr")
public class OcrController {
    private final OcrService ocrService;
    private final MedicineService medicineService;

    public OcrController(OcrService ocrService, MedicineService medicineService) {
        this.ocrService = ocrService;
        this.medicineService = medicineService;
    }

    @PostMapping("/upload")
    public ApiResponse<OcrDtos.UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("upload success", ocrService.upload(AuthContext.requireUserId(), file));
    }

    @GetMapping("/file/{storedName}")
    public ResponseEntity<Resource> file(@PathVariable String storedName) {
        OcrService.FileData file = ocrService.loadFile(storedName);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.contentType != null && !file.contentType.trim().isEmpty()) {
            mediaType = MediaType.parseMediaType(file.contentType);
        }
        return ResponseEntity.ok().contentType(mediaType).body(file.resource);
    }

    @PostMapping("/recognize")
    public ApiResponse<OcrDtos.MedicineInfoResponse> recognize(@Valid @RequestBody OcrDtos.RecognizeRequest request) {
        return ApiResponse.success("recognize success", ocrService.recognize(request.userId, request.imageUrl));
    }

    @PostMapping("/save")
    public ApiResponse<OcrDtos.MedicineSaveResponse> save(@Valid @RequestBody OcrDtos.MedicineSaveRequest request) {
        return ApiResponse.success("save success", medicineService.createFromOcr(request));
    }
}
