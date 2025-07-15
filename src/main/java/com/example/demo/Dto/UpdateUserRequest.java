package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

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
