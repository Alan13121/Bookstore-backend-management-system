package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入或刷新 token 後的回應。
 *
 * <p>前端拿到後要存起來，之後每次呼叫 API 都在 Header 帶上
 * {@code Authorization: Bearer <token>}。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TokenResponse", description = "JWT token 回應")
public class TokenResponse {

    @Schema(description = "JWT Token 字串", example = "eyJhbGciOiJIUzI1...")
    private String token;
}
