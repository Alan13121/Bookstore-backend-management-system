package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登入成功後回傳給前端的 DTO，只包含 JWT token。
 * <p>{@code @AllArgsConstructor} 讓我們可以直接 {@code new AuthResponse(token)}。</p>
 */
@Data
@AllArgsConstructor
@Schema(name = "AuthResponse", description = "登入回應，回傳 JWT token")
public class AuthResponse {
    @Schema(description = "JWT Token 字串", example = "eyJhbGciOiJIUzI1...")
    private String token;
}
