package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 更新權限規則的請求內容，要更新哪一筆由網址上的 id 決定。 */
@Data
@Schema(name = "UrlRoleMappingUpdateRequest", description = "更新角色訪問權限請求")
public class UrlRoleMappingUpdateRequest {

    @Schema(description = "URL 比對規則", example = "/api/books/.*")
    private String urlPattern;

    @Schema(description = "允許角色，逗號分隔", example = "ADMIN,STAFF")
    private String roles;
}
