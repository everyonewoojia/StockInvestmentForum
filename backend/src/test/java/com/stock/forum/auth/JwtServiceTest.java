package com.stock.forum.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.common.ApiException;
import com.stock.forum.config.AppProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void rejectsMalformedToken() {
        JwtService jwtService = jwtService(10);

        assertThatThrownBy(() -> jwtService.parseUserId("not-a-jwt"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid token")
                .extracting("code")
                .isEqualTo(401);
    }

    @Test
    void rejectsTamperedSignature() {
        JwtService jwtService = jwtService(10);
        String token = jwtService.createToken(7L);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parseUserId(tampered))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid token")
                .extracting("code")
                .isEqualTo(401);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService jwtService = jwtService(-1);
        String token = jwtService.createToken(7L);

        assertThatThrownBy(() -> jwtService.parseUserId(token))
                .isInstanceOf(ApiException.class)
                .hasMessage("Token expired")
                .extracting("code")
                .isEqualTo(401);
    }

    private JwtService jwtService(long expirationMinutes) {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret("unit-test-secret-that-is-long-enough");
        properties.getJwt().setExpirationMinutes(expirationMinutes);
        return new JwtService(properties, new ObjectMapper());
    }
}
