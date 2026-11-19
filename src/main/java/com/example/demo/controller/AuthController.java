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

/**
 * 認證 API：登入、註冊、重設密碼、token 相關。
 *
 * <p>{@code /api/auth/} 開頭的路徑都在 DynamicAuthorizationFilter 的白名單中，不需要登入就能呼叫。
 * 但 refresh-token 與 check 實際上需要帶 token，否則 Service 會丟出未登入的例外（401）。</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "入口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 登入：帳密正確時回傳 {@code {"token": "..."}}，前端之後每次呼叫 API 都要在 Header 帶上它。 */
    @Operation(summary = "登入取得token")
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        return new AuthResponse(authService.login(authRequest));
    }

    /** 註冊：帳號重複時回 409。 */
    @Operation(summary = "註冊新用戶")
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "註冊成功";
    }

    /** 重設密碼（⚠ 目前沒有身分驗證，見 AuthService.resetPassword 的說明）。 */
    @Operation(summary = "重設密碼")
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return "密碼已重設";
    }

    /**
     * 重新簽發 Token。
     * 方法參數宣告 {@code Authentication} 時，Spring 會自動注入目前 SecurityContext 中的登入者。
     */
    @Operation(summary = "重新簽發 Token", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/refresh-token")
    public AuthResponse refreshToken(Authentication authentication) {
        return new AuthResponse(authService.refreshToken(authentication));
    }

    /** 驗證登入狀態：回傳登入者帳號與權限。 */
    @Operation(summary = "驗證登入狀態", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/check")
    public Map<String, Object> checkToken(Authentication authentication) {
        return authService.checkToken(authentication);
    }
}
