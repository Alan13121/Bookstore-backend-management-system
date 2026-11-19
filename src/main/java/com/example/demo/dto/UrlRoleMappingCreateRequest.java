package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 新增權限規則的請求內容。
 *
 * <p>用 DTO 而不是直接收 Entity，可以避免前端在 body 裡偷帶 id 去覆蓋既有規則。</p>
 */
@Data
@Schema(name = "UrlRoleMappingCreateRequest", description = "新增角色訪問權限請求")
public class UrlRoleMappingCreateRequest {

    @Schema(description = "URL 比對規則", example = "/api/books/.*")
    private String urlPattern;

    @Schema(description = "允許角色，逗號分隔", example = "ADMIN,STAFF")
    private String roles;
}
