package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@Tag(name = "用戶管理", description = "處理用戶的增刪查改")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查詢所有啟用中的用戶")
    @GetMapping("/active")
    public List<User> getAllActiveUsers() {
        return userService.getAllActiveUsers();
    }

    @Operation(summary = "依帳號關鍵字搜尋用戶")
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        return userService.searchUsersByUsername(keyword);
    }

    @Operation(summary = "取得用戶的角色字串")
    @GetMapping("/{id}/roles")
    public String getRolesByIdToString(
            @Parameter(description = "用戶ID") @PathVariable Integer id) {
        return userService.getRolesByIdToString(id);
    }

    @Operation(summary = "查詢所有用戶")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "查詢單個用戶")
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @Parameter(description = "用戶ID") @PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "新增用戶")
    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "更新用戶資料")
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "刪除用戶")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用戶ID") @PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
