package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TokenResponse", description = "JWT token 回應")
public class TokenResponse {

    @Schema(description = "JWT Token 字串", example = "eyJhbGciOiJIUzI1...")
    private String token;
}
