package com.example.demo.controller;

import com.example.demo.Dto.AuthRequest;
import com.example.demo.Dto.AuthResponse;
import com.example.demo.Dto.RegisterRequest;
import com.example.demo.Dto.ResetPasswordRequest;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "入口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "登入取得token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        String token = authService.login(authRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "註冊新用戶")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("註冊成功");
    }

    @Operation(summary = "重設密碼")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("密碼已重設");
    }

    @Operation(summary = "重新簽發 Token", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        String token = authService.refreshToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "驗證登入狀態", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/check")
    public ResponseEntity<?> checkToken(Authentication authentication) {
        return ResponseEntity.ok(authService.checkToken(authentication));
    }
}
