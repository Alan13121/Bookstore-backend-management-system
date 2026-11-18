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

import com.example.demo.dto.RoleCreateRequest;
import com.example.demo.dto.RoleResponse;
import com.example.demo.dto.RoleUpdateRequest;
import com.example.demo.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "角色管理", description = "角色的增刪查改")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "查詢所有角色")
    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @Operation(summary = "查詢單個角色")
    @GetMapping("/{id}")
    public RoleResponse getRoleById(@Parameter(description = "角色 ID") @PathVariable Integer id) {
        return roleService.getRoleById(id);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public RoleResponse createRole(@RequestBody RoleCreateRequest request) {
        return roleService.createRole(request);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public RoleResponse updateRole(@Parameter(description = "角色 ID") @PathVariable Integer id,
                                   @RequestBody RoleUpdateRequest request) {
        return roleService.updateRole(id, request);
    }

    @Operation(summary = "刪除角色")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@Parameter(description = "角色 ID") @PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
