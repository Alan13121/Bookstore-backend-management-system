package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 建立或更新角色的請求 DTO（POST / PUT /api/roles）。
 */
@Data
@Schema(description = "建立或更新角色請求")
public class RoleCreateRequest {
    @Schema(description = "角色名稱", example = "ROLE_STAFF")
    private String name;
}
