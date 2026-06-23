package com.medicine.assistant.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.assistant.config.AppProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void createsAndParsesToken() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret("unit-test-secret-that-is-long-enough");
        properties.getJwt().setExpirationMinutes(10);
        JwtService jwtService = new JwtService(properties, new ObjectMapper());

        String token = jwtService.createToken(42L);

        assertThat(jwtService.parseUserId(token)).isEqualTo(42L);
    }
}
