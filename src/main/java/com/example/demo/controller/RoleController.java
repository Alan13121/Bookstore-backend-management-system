package com.example.demo.controller;

import com.example.demo.Dto.RoleCreateRequest;
import com.example.demo.Dto.RoleDto;
import com.example.demo.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return roleService.findRoleResponse(id);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleCreateRequest request) {
        return roleService.createRoleResponse(request);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Integer id,
                                              @RequestBody RoleCreateRequest request) {
        return roleService.updateRoleResponse(id, request);
    }

    @Operation(summary = "刪除角色")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        return roleService.deleteRoleResponse(id);
    }
}
