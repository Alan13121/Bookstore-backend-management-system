package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 更新角色的請求內容。
 *
 * <p>目前欄位與 {@link RoleCreateRequest} 相同，分開寫是為了之後新增和更新的規則
 * （例如驗證條件）可以各自調整而不互相影響。</p>
 */
@Data
@Schema(name = "RoleUpdateRequest", description = "更新角色請求")
public class RoleUpdateRequest {

    @Schema(description = "角色名稱", example = "STAFF")
    private String name;
}
