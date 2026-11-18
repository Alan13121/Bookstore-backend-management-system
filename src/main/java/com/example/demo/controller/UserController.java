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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用戶管理", description = "用戶的增刪查改")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查詢所有用戶")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "查詢所有啟用中的用戶")
    @GetMapping("/active")
    public List<UserResponse> getAllActiveUsers() {
        return userService.getAllActiveUsers();
    }

    @Operation(summary = "依用戶名關鍵字搜尋用戶")
    @GetMapping("/search")
    public List<UserResponse> searchUsers(@Parameter(description = "用戶名關鍵字") @RequestParam String keyword) {
        return userService.searchUsersByUsername(keyword);
    }

    @Operation(summary = "查詢單個用戶")
    @GetMapping("/{id}")
    public UserResponse getUserById(@Parameter(description = "用戶 ID") @PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "取得用戶的角色名稱（逗號分隔）")
    @GetMapping("/{id}/roles")
    public String getUserRoleNames(@Parameter(description = "用戶 ID") @PathVariable Integer id) {
        return userService.getUserRoleNames(id);
    }

    @Operation(summary = "新增用戶")
    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "更新用戶")
    @PutMapping("/{id}")
    public UserResponse updateUser(@Parameter(description = "用戶 ID") @PathVariable Integer id,
                                   @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "刪除用戶")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "用戶 ID") @PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
