package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 回傳給前端的權限規則（含 id，給管理頁面編輯 / 刪除用）。 */
@Data
@Schema(name = "UrlRoleMappingResponse", description = "角色訪問權限")
public class UrlRoleMappingResponse {

    @Schema(description = "規則 ID", example = "1")
    private Long id;

    @Schema(description = "URL 比對規則", example = "/api/books/.*")
    private String urlPattern;

    @Schema(description = "允許角色，逗號分隔", example = "ADMIN,STAFF")
    private String roles;
}
