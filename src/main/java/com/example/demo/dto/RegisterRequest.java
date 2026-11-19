package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.ToString;

/**
 * 一般使用者自行註冊的請求內容（POST /api/auth/register）。
 *
 * <p>不能自己指定角色，註冊後一律給 STAFF（見 {@code AuthService.register}）。</p>
 */
@Data
@Schema(name = "RegisterRequest", description = "註冊新用戶請求")
public class RegisterRequest {

    @Schema(description = "用戶名", example = "staff01")
    private String username;

    @Schema(description = "密碼", example = "123456")
    @ToString.Exclude // 不讓密碼出現在 toString()，否則記錄參數的 log 會印出明碼
    private String password;

    @Schema(description = "全名", example = "ANDY NOODLE")
    private String fullName;

    @Schema(description = "電子郵件", example = "staff01@example.com")
    private String email;

    @Schema(description = "電話", example = "0912345678")
    private String phone;
}
