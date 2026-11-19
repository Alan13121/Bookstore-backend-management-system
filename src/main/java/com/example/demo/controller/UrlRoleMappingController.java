package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PublicUrlRoleMappingResponse;
import com.example.demo.dto.UrlRoleMappingCreateRequest;
import com.example.demo.dto.UrlRoleMappingResponse;
import com.example.demo.dto.UrlRoleMappingUpdateRequest;
import com.example.demo.service.UrlRoleMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 動態權限規則 API：管理員在這裡設定「哪個網址允許哪些角色」。
 *
 * <p>修改後下一個 request 就會生效，因為 DynamicAuthorizationFilter 每次都從資料庫讀取規則。
 * 這個路徑也符合 {@code /api/roles/.*} 規則，所以預設只有 ADMIN 能存取；只有 /public 在白名單中。</p>
 */
@RestController
@RequestMapping("/api/roles/mappings")
@Tag(name = "角色權限管理", description = "動態訪問權限的增刪查改")
@SecurityRequirement(name = "bearerAuth")
public class UrlRoleMappingController {

    private final UrlRoleMappingService urlRoleMappingService;

    public UrlRoleMappingController(UrlRoleMappingService urlRoleMappingService) {
        this.urlRoleMappingService = urlRoleMappingService;
    }

    @Operation(summary = "查詢所有角色訪問權限")
    @GetMapping
    public List<UrlRoleMappingResponse> getAllMappings() {
        return urlRoleMappingService.getAllMappings();
    }

    @Operation(summary = "查詢公開版角色訪問權限（前端選單用，免登入）")
    @GetMapping("/public")
    public List<PublicUrlRoleMappingResponse> getPublicMappings() {
        return urlRoleMappingService.getPublicMappings();
    }

    @Operation(summary = "新增角色訪問權限")
    @PostMapping
    public UrlRoleMappingResponse createMapping(@RequestBody UrlRoleMappingCreateRequest request) {
        return urlRoleMappingService.createMapping(request);
    }

    @Operation(summary = "更新角色訪問權限")
    @PutMapping("/{id}")
    public UrlRoleMappingResponse updateMapping(@Parameter(description = "規則 ID") @PathVariable Long id,
                                                @RequestBody UrlRoleMappingUpdateRequest request) {
        return urlRoleMappingService.updateMapping(id, request);
    }

    @Operation(summary = "刪除角色訪問權限")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@Parameter(description = "規則 ID") @PathVariable Long id) {
        urlRoleMappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
}
