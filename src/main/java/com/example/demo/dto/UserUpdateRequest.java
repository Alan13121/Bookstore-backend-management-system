package com.example.demo.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 更新用戶的請求內容，採「部分更新」：欄位為 null 代表不修改。
 *
 * <p>這裡沒有密碼欄位，密碼另外透過重設密碼流程處理。</p>
 */
@Data
@Schema(name = "UserUpdateRequest", description = "更新用戶請求（欄位為 null 表示不修改）")
public class UserUpdateRequest {

    @Schema(description = "用戶名", example = "jerry")
    private String username;

    @Schema(description = "全名", example = "Jerry Chen")
    private String fullName;

    @Schema(description = "電話", example = "0912345678")
    private String phone;

    @Schema(description = "電子郵件", example = "jerry@example.com")
    private String email;

    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "角色 ID 列表", example = "[1, 2]")
    private Set<Integer> roleIds;
}
