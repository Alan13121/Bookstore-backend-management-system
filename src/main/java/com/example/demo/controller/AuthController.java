package com.example.demo.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "入口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登入取得token")
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        return new AuthResponse(authService.login(authRequest));
    }

    @Operation(summary = "註冊新用戶")
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "註冊成功";
    }

    @Operation(summary = "重設密碼")
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return "密碼已重設";
    }

    @Operation(summary = "重新簽發 Token", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/refresh-token")
    public AuthResponse refreshToken(Authentication authentication) {
        return new AuthResponse(authService.refreshToken(authentication));
    }

    @Operation(summary = "驗證登入狀態", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/check")
    public Map<String, Object> checkToken(Authentication authentication) {
        return authService.checkToken(authentication);
    }
}
