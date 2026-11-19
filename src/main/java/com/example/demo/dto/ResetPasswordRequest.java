package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 重設密碼請求 DTO（POST /api/auth/reset-password）。
 * <p>⚠ 目前只要知道 username 就能重設密碼，實務上需要再驗證身分
 * （例如舊密碼、Email 驗證碼或一次性重設 token）。</p>
 */
@Data
@Schema(name = "ResetPasswordRequest", description = "重設密碼請求")
public class ResetPasswordRequest {
    @Schema(description = "用戶名", example = "staff01")
    private String username;

    @Schema(description = "新密碼", example = "newPassword123")
    private String newPassword;
}
