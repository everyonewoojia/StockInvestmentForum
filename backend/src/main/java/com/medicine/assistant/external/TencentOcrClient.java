package com.medicine.assistant.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.assistant.common.ApiException;
import com.medicine.assistant.config.AppProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class TencentOcrClient {
    private static final String ENDPOINT = "https://ocr.tencentcloudapi.com";
    private static final String HOST = "ocr.tencentcloudapi.com";
    private static final String SERVICE = "ocr";
    private static final String ACTION = "GeneralBasicOCR";
    private static final String VERSION = "2018-11-19";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AppProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TencentOcrClient(AppProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<String> recognizeByBase64(String imageBase64) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("ImageBase64", imageBase64);
        return request(payload);
    }

    public List<String> recognizeByUrl(String imageUrl) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("ImageUrl", imageUrl);
        return request(payload);
    }

    private List<String> request(Map<String, Object> payload) {
        if (!StringUtils.hasText(properties.getTencent().getSecretId()) || !StringUtils.hasText(properties.getTencent().getSecretKey())) {
            throw ApiException.serverError("Tencent OCR credentials are not configured");
        }
        try {
            String body = objectMapper.writeValueAsString(payload);
            long timestamp = Instant.now().getEpochSecond();
            String date = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC).format(DATE);
            String authorization = sign(body, timestamp, date);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/json; charset=utf-8"));
            headers.set("Host", HOST);
            headers.set("X-TC-Action", ACTION);
            headers.set("X-TC-Timestamp", String.valueOf(timestamp));
            headers.set("X-TC-Version", VERSION);
            headers.set("X-TC-Region", properties.getTencent().getRegion());
            headers.set("Authorization", authorization);

            ResponseEntity<String> response = restTemplate.postForEntity(ENDPOINT, new HttpEntity<String>(body, headers), String.class);
            return parseResponse(response.getBody());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.serverError("Tencent OCR request failed");
        }
    }

    private String sign(String payload, long timestamp, String date) throws Exception {
        String hashedPayload = sha256Hex(payload);
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + HOST + "\n"
                + "x-tc-action:" + ACTION.toLowerCase(Locale.ROOT) + "\n";
        String canonicalRequest = "POST\n"
                + "/\n"
                + "\n"
                + canonicalHeaders
                + "\n"
                + "content-type;host;x-tc-action\n"
                + hashedPayload;
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String stringToSign = ALGORITHM + "\n"
                + timestamp + "\n"
                + credentialScope + "\n"
                + sha256Hex(canonicalRequest);

        byte[] secretDate = hmac256(("TC3" + properties.getTencent().getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, SERVICE);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = bytesToHex(hmac256(secretSigning, stringToSign));

        return ALGORITHM
                + " Credential=" + properties.getTencent().getSecretId() + "/" + credentialScope
                + ", SignedHeaders=content-type;host;x-tc-action"
                + ", Signature=" + signature;
    }

    private List<String> parseResponse(String responseBody) throws Exception {
        Map<String, Object> root = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> response = castMap(root.get("Response"));
        if (response == null) {
            throw ApiException.serverError("Invalid Tencent OCR response");
        }
        Map<String, Object> error = castMap(response.get("Error"));
        if (error != null) {
            Object message = error.get("Message");
            throw ApiException.serverError("Tencent OCR error: " + String.valueOf(message));
        }
        Object detectionsValue = response.get("TextDetections");
        List<String> lines = new ArrayList<String>();
        if (detectionsValue instanceof List) {
            for (Object item : (List<?>) detectionsValue) {
                Map<String, Object> detection = castMap(item);
                if (detection != null && detection.get("DetectedText") != null) {
                    lines.add(String.valueOf(detection.get("DetectedText")));
                }
            }
        }
        return lines;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private String sha256Hex(String input) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        return bytesToHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] hmac256(byte[] key, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}
