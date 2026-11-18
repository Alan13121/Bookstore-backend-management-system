package com.example.demo.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(name = "UserCreateRequest", description = "新增用戶請求")
public class UserCreateRequest {

    @Schema(description = "用戶名", example = "jerry")
    private String username;

    @Schema(description = "密碼", example = "123456")
    private String password;

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
