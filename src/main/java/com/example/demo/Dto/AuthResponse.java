package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "AuthResponse", description = "登入回應，回傳 JWT token")
public class AuthResponse {
    @Schema(description = "JWT Token 字串", example = "eyJhbGciOiJIUzI1...")
    private String token;
}
