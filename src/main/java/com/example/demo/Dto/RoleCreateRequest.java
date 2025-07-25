package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "建立或更新角色請求")
public class RoleCreateRequest {
    @Schema(description = "角色名稱", example = "ROLE_STAFF")
    private String name;
}
