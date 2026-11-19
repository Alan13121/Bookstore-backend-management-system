package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.ToString;

/** 登入的請求內容（POST /api/auth/login）。 */
@Data
@Schema(name = "LoginRequest", description = "登入請求")
public class LoginRequest {

    @Schema(description = "用戶名", example = "admin")
    private String username;

    @Schema(description = "密碼", example = "123456")
    @ToString.Exclude // 不讓密碼出現在 toString()，否則記錄參數的 log 會印出明碼
    private String password;
}
