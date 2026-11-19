package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公開版的 URL ↔ 角色 規則（不含 id），給前端決定要顯示哪些選單。
 */
@Data
@Schema(name = "PublicRoleMappingDto", description = "公開版角色訪問規則")
@NoArgsConstructor
@AllArgsConstructor
public class PublicRoleMappingDto {
    @Schema(description = "URL 比對規則", example = "/api/books/.*")
    private String urlPattern;
    @Schema(description = "允許角色，逗號分隔", example = "ADMIN,STAFF")
    private String roles;
}
