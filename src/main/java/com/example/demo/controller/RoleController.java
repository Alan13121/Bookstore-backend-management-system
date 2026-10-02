package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.RoleCreateRequest;
import com.example.demo.dto.RoleDto;
import com.example.demo.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/roles")
@Tag(name = "角色管理", description = "角色的增查改刪")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "查詢所有角色")
    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles();
    }

    @Operation(summary = "查詢一個角色")
    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Integer id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public RoleDto createRole(@RequestBody RoleCreateRequest request) {
        return roleService.createRole(request);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public RoleDto updateRole(@PathVariable Integer id,
                              @RequestBody RoleCreateRequest request) {
        return roleService.updateRole(id, request);
    }

    @Operation(summary = "刪除角色")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
