package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(name = "RoleUpdateRequest", description = "更新角色請求")
public class RoleUpdateRequest {

    @Schema(description = "角色名稱", example = "STAFF")
    private String name;
}
