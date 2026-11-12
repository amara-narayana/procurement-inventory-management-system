package com.zycus.pims.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "VerySecretKeyForTestingPurposeMustBeAtLeast256BitsLongAndSecure123!";
    private static final long EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        String username = "john.doe";
        Long userId = 42L;
        List<String> roles = List.of("EMPLOYEE", "APPROVER");

        String token = jwtTokenProvider.generateToken(username, userId, roles);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo(username);
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.extractRoles(token)).containsExactlyInAnyOrder("EMPLOYEE", "APPROVER");
    }

    @Test
    void shouldGenerateRefreshToken() {
        String username = "jane.smith";
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.extractUsername(refreshToken)).isEqualTo(username);
    }

    @Test
    void shouldFailValidationOnInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.structure")).isFalse();
    }
}
