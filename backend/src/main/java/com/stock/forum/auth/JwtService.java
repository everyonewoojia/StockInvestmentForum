package com.stock.forum.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.common.ApiException;
import com.stock.forum.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final String HMAC_ALG = "HmacSHA256";
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(AppProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String createToken(Long userId) {
        long now = Instant.now().getEpochSecond();
        long exp = now + properties.getJwt().getExpirationMinutes() * 60L;
        Map<String, Object> header = new LinkedHashMap<String, Object>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("sub", String.valueOf(userId));
        payload.put("iat", now);
        payload.put("exp", exp);

        String headerPart = base64Url(toJsonBytes(header));
        String payloadPart = base64Url(toJsonBytes(payload));
        String signingInput = headerPart + "." + payloadPart;
        return signingInput + "." + base64Url(hmac(signingInput));
    }

    public Long parseUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw ApiException.unauthorized("Invalid token");
            }
            String signingInput = parts[0] + "." + parts[1];
            String expected = base64Url(hmac(signingInput));
            if (!constantTimeEquals(expected, parts[2])) {
                throw ApiException.unauthorized("Invalid token");
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {
            });
            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                throw ApiException.unauthorized("Token expired");
            }
            return Long.valueOf(String.valueOf(payload.get("sub")));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.unauthorized("Invalid token");
        }
    }

    private byte[] toJsonBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception ex) {
            throw ApiException.serverError("Failed to create token");
        }
    }

    private byte[] hmac(String input) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            byte[] secret = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(secret, HMAC_ALG));
            return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw ApiException.serverError("Failed to sign token");
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        int result = left.length() ^ right.length();
        int max = Math.max(left.length(), right.length());
        for (int i = 0; i < max; i++) {
            char a = i < left.length() ? left.charAt(i) : 0;
            char b = i < right.length() ? right.charAt(i) : 0;
            result |= a ^ b;
        }
        return result == 0;
    }
}
