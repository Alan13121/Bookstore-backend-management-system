package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.TokenCheckResponse;
import com.example.demo.dto.TokenResponse;
import com.example.demo.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "認證", description = "登入、註冊與 token 管理")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登入取得 token")
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "註冊新用戶")
    @PostMapping("/register")
    public MessageResponse register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("註冊成功");
    }

    @Operation(summary = "重設密碼")
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("密碼已重設");
    }

    @Operation(summary = "重新簽發 token", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/refresh-token")
    public TokenResponse refreshToken(Authentication authentication) {
        return authService.refreshToken(authentication);
    }

    @Operation(summary = "驗證登入狀態", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/check")
    public TokenCheckResponse checkToken(Authentication authentication) {
        return authService.checkToken(authentication);
    }
}
