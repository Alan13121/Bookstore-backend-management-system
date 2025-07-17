package com.example.demo.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider();

        // 利用反射設置 private 字段
        Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenProvider, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdef"); // 64 字元

        Field expField = JwtTokenProvider.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtTokenProvider, 3600000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        User userDetails = new User(
                "testuser",
                "password",
                List.of(() -> "ROLE_USER", () -> "ROLE_ADMIN")
        );

        String token = jwtTokenProvider.generateToken(userDetails);
        System.out.println("Generated Token: " + token);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token), "Token should be valid");

        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testuser", username);

        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "this.is.an.invalid.token";

        assertFalse(jwtTokenProvider.validateToken(invalidToken), "Invalid token should fail");

        assertThrows(JwtException.class, () -> jwtTokenProvider.getUsernameFromToken(invalidToken));
        assertThrows(JwtException.class, () -> jwtTokenProvider.getRolesFromToken(invalidToken));
    }

    @Test
    void testShortSecretKeyThrowsException() {
        JwtTokenProvider provider = new JwtTokenProvider();

        assertThrows(IllegalArgumentException.class, () -> {
            Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(provider, "short-key");
            Field expField = JwtTokenProvider.class.getDeclaredField("jwtExpirationMs");
            expField.setAccessible(true);
            expField.set(provider, 3600000L);

            provider.generateToken(new User("user", "pass", List.of()));
        });
    }
}
