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

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/roles/mappings")
@Tag(name = "角色權限管理", description = "動態訪問權限更改")
public class RoleMappingController {

    private final UrlRoleMappingService urlRoleMappingService;

    public RoleMappingController(UrlRoleMappingService urlRoleMappingService) {
        this.urlRoleMappingService = urlRoleMappingService;
    }

    @GetMapping
    @Operation(summary = "查詢所有角色訪問權限")
    public List<UrlRoleMapping> getAll() {
        return urlRoleMappingService.getAll();
    }

    @PostMapping
    @Operation(summary = "新增角色訪問權限")
    public UrlRoleMapping save(@RequestBody UrlRoleMapping mapping) {
        return urlRoleMappingService.save(mapping);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色訪問權限")
    public UrlRoleMapping update(@PathVariable Long id, @RequestBody UrlRoleMapping mapping) {
        mapping.setId(id);
        return urlRoleMappingService.save(mapping);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除角色訪問權限")
    public void delete(@PathVariable Long id) {
        urlRoleMappingService.delete(id);
    }

    @GetMapping("/public")
    @Operation(summary = "公開版角色規則（前端選單用）")
    public List<PublicRoleMappingDto> getPublicMappings() {
        return urlRoleMappingService.getAll().stream()
            .map(mapping -> new PublicRoleMappingDto(
                mapping.getUrlPattern(),
                mapping.getRoles()
            ))
            .collect(Collectors.toList());
    }

}
