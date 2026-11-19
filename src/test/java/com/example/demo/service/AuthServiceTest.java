package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.TokenCheckResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void login() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("6969");

        String token = authService.login(request).getToken();

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("admin", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @Transactional
    void register() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setFullName("測試用戶");
        request.setEmail("test@example.com");
        request.setPhone("0900000000");

        authService.register(request);

        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    @Transactional
    void registerDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    @Transactional
    void resetPassword() {
        String oldHash = findUser("admin").getPassword();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername("admin");
        request.setNewPassword("222222");

        authService.resetPassword(request);

        assertNotEquals(oldHash, findUser("admin").getPassword());
    }

    @Test
    void refreshToken() {
        String token = authService.refreshToken(authenticated(findUser("admin"))).getToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void checkToken() {
        TokenCheckResponse result = authService.checkToken(authenticated(findUser("admin")));

        assertEquals("admin", result.getUsername());
        assertTrue(result.getAuthorities().contains("ROLE_ADMIN"));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    private Authentication authenticated(User user) {
        Authentication authentication = new TestingAuthenticationToken(user, null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        return authentication;
    }
}
