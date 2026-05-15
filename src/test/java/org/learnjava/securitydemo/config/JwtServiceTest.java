package org.learnjava.securitydemo.config;

import org.junit.jupiter.api.Test;
import org.learnjava.securitydemo.user.Role;
import org.learnjava.securitydemo.user.User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET_KEY));

    @Test
    void generateTokenCanExtractUsernameAndExtraClaims() {
        User user = User.builder()
                .email("aly@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(Map.of("tenant", "demo"), user);
        String tenant = jwtService.extractClaim(token, claims -> claims.get("tenant", String.class));

        assertThat(jwtService.extractUsername(token)).isEqualTo("aly@example.com");
        assertThat(tenant).isEqualTo("demo");
    }

    @Test
    void tokenIsValidForMatchingUser() {
        User user = User.builder()
                .email("aly@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        User tokenOwner = User.builder()
                .email("aly@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        User differentUser = User.builder()
                .email("mona@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, differentUser)).isFalse();
    }
}
