package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 登入請求 DTO（POST /api/auth/login 的 request body）。
 * <p>DTO（Data Transfer Object）只負責「搬資料」，用來隔開前端格式與資料庫 Entity，
 * 避免前端直接操作 Entity 欄位（例如 id、enabled、roles）。</p>
 */
@Data
@Schema(name = "AuthRequest", description = "登入請求")
public class AuthRequest {
    @Schema(description = "用戶名", example = "admin")
    private String username;

    @Schema(description = "密碼", example = "123456")
    private String password;
}
