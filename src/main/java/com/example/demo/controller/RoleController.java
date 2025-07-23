package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.service.UrlRoleMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/roles")
@Tag(name = "角色權限管理", description = "動態訪問權限更改")
public class RoleController {

    @Autowired
    private UrlRoleMappingService service;

    @GetMapping("/mappings")
    @Operation(summary = "查詢所有角色及訪問權限")
    public List<UrlRoleMapping> getAll() {
        return service.getAll();
    }

    @PostMapping("/mappings")
    @Operation(summary = "新增角色及訪問權限")
    public UrlRoleMapping save(@RequestBody UrlRoleMapping mapping) {
        return service.save(mapping);
    }

    @PutMapping("/mappings/{id}")
    @Operation(summary = "更新角色及訪問權限")
    public UrlRoleMapping update(@PathVariable Long id, @RequestBody UrlRoleMapping mapping) {
        mapping.setId(id);
        return service.save(mapping);
    }

    @DeleteMapping("/mappings/{id}")
    @Operation(summary = "刪除角色及訪問權限")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}


