package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色資料")
public class RoleDto {
    @Schema(description = "角色 ID", example = "1")
    private Integer id;

    @Schema(description = "角色名稱", example = "ROLE_ADMIN")
    private String name;
}

