package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 回傳給前端的角色資料。 */
@Data
@Schema(name = "RoleResponse", description = "角色資料")
public class RoleResponse {

    @Schema(description = "角色 ID", example = "1")
    private Integer id;

    @Schema(description = "角色名稱", example = "ADMIN")
    private String name;
}
