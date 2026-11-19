package com.example.demo.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 更新用戶的請求 DTO（PUT /api/users/{id}）。
 * <p>採「部分更新」：欄位為 null 代表不修改（見 {@code UserService.updateUser}）。
 * 這裡不包含密碼，密碼另外透過重設密碼流程處理。</p>
 */
@Data
@Schema(description = "用戶更新請求資料")
public class UpdateUserRequest {

    @Schema(description = "暱稱", example = "Jerry")
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
