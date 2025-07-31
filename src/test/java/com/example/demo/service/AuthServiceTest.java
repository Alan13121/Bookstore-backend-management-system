package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dto.AuthRequest;
import com.example.demo.Dto.RegisterRequest;
import com.example.demo.Dto.ResetPasswordRequest;
import com.example.demo.entity.User;
import com.example.demo.util.JwtTokenProvider;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    public void login(){
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("6969");

        String token = authService.login(request);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(request.getUsername(), jwtTokenProvider.getUsernameFromToken(token));
        
    }

    @Test
    @Transactional
    public void register() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("123456");
        req.setFullName("測試用戶");
        req.setEmail("test@example.com");
        req.setPhone("0900000000");

        authService.register(req);

        List<User> u = userService.searchUsersByUsername("newuser");
        assertNotNull(u);
        assertEquals("newuser", u.get(0).getUsername());
    }

    @Test
    @Transactional
    public void resetPassword() {
        ResetPasswordRequest resetReq = new ResetPasswordRequest();
        resetReq.setUsername("admin");
        resetReq.setNewPassword("222222");

        authService.resetPassword(resetReq);

        assertDoesNotThrow(() -> {
            User u = userService.searchUsersByUsername("admin").get(0);
            //密碼會加密 無法直接比對
            assertNotNull(u.getPassword());
        });
    }

    @Test
    public void refreshToken() {
        // 模擬一個已驗證的使用者
        User user = userService.searchUsersByUsername("admin").get(0);
        Authentication authentication = new TestingAuthenticationToken(user, null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        String token = authService.refreshToken(authentication);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void checkToken() {
        User user = userService.searchUsersByUsername("admin").get(0);
        Authentication authentication = new TestingAuthenticationToken(user, null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        Map<String, Object> result = authService.checkToken(authentication);
        assertNotNull(result);
        assertEquals("admin", result.get("username"));
    }
}
