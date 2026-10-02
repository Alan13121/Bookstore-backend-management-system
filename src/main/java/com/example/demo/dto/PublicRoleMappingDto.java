package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
