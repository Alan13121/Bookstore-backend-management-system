package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.PublicRoleMappingDto;
import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.service.UrlRoleMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 動態權限規則 API：管理員可以在這裡新增 / 修改「哪個網址允許哪些角色」，
 * 修改後下一個 request 就會生效（因為 DynamicAuthorizationFilter 每次都從資料庫讀取規則）。
 *
 * <p>路徑 /api/roles/mappings 也符合 /api/roles/.* 規則，所以預設只有 ADMIN 能存取；
 * 只有 /public 在白名單中。</p>
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/roles/mappings")
@Tag(name = "角色權限管理", description = "動態訪問權限更改")
public class RoleMappingController {

    private final UrlRoleMappingService urlRoleMappingService;

    public RoleMappingController(UrlRoleMappingService urlRoleMappingService) {
        this.urlRoleMappingService = urlRoleMappingService;
    }

    @Operation(summary = "查詢所有角色訪問權限")
    @GetMapping
    public List<UrlRoleMapping> getAll() {
        return urlRoleMappingService.getAll();
    }

    /** ⚠ 這裡直接用 Entity 當 request body，前端若傳入 id 就會變成覆蓋既有規則，建議改用 DTO。 */
    @Operation(summary = "新增角色訪問權限")
    @PostMapping
    public UrlRoleMapping save(@RequestBody UrlRoleMapping mapping) {
        return urlRoleMappingService.save(mapping);
    }

    /** 以網址上的 id 為準，避免 body 裡的 id 與網址不一致。 */
    @Operation(summary = "更新角色訪問權限")
    @PutMapping("/{id}")
    public UrlRoleMapping update(@PathVariable Long id, @RequestBody UrlRoleMapping mapping) {
        mapping.setId(id);
        return urlRoleMappingService.save(mapping);
    }

    /** 回傳型別為 void 時，成功的狀態碼是 200（不是 204）。 */
    @Operation(summary = "刪除角色訪問權限")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        urlRoleMappingService.delete(id);
    }

    /** 不需登入（在白名單中）。前端用它判斷目前使用者可以看到哪些選單。 */
    @Operation(summary = "公開版角色規則（前端選單用）")
    @GetMapping("/public")
    public List<PublicRoleMappingDto> getPublicMappings() {
        return urlRoleMappingService.getAll().stream()
            .map(mapping -> new PublicRoleMappingDto(
                mapping.getUrlPattern(),
                mapping.getRoles()
            ))
            .collect(Collectors.toList());
    }

}
