package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 新增角色的請求內容。角色名稱不需要加 {@code ROLE_} 前綴。 */
@Data
@Schema(name = "RoleCreateRequest", description = "新增角色請求")
public class RoleCreateRequest {

    @Schema(description = "角色名稱", example = "STAFF")
    private String name;
}
