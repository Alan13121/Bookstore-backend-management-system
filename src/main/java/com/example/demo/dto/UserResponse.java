package com.example.demo.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 回傳給前端的用戶資料。
 *
 * <p>刻意不含 password，避免密碼雜湊外洩；roles 只回傳角色名稱。</p>
 */
@Data
@Schema(name = "UserResponse", description = "用戶資料（不含密碼）")
public class UserResponse {

    @Schema(description = "用戶 ID", example = "1")
    private Integer id;

    @Schema(description = "用戶名", example = "admin")
    private String username;

    @Schema(description = "全名", example = "Jerry Chen")
    private String fullName;

    @Schema(description = "電話", example = "0912345678")
    private String phone;

    @Schema(description = "電子郵件", example = "jerry@example.com")
    private String email;

    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "角色名稱清單", example = "[\"ADMIN\"]")
    private Set<String> roles;
}
