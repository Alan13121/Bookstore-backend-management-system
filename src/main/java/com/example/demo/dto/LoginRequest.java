package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 登入的請求內容（POST /api/auth/login）。 */
@Data
@Schema(name = "LoginRequest", description = "登入請求")
public class LoginRequest {

    @Schema(description = "用戶名", example = "admin")
    private String username;

    @Schema(description = "密碼", example = "123456")
    private String password;
}
