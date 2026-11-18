package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(name = "PublicUrlRoleMappingResponse", description = "公開版角色訪問權限（不含 ID）")
public class PublicUrlRoleMappingResponse {

    @Schema(description = "URL 比對規則", example = "/api/books/.*")
    private String urlPattern;

    @Schema(description = "允許角色，逗號分隔", example = "ADMIN,STAFF")
    private String roles;
}
