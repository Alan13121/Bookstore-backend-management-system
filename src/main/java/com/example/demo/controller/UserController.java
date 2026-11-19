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

/** 用戶管理 API（預設規則：僅 ADMIN 可存取）。 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@Tag(name = "用戶管理", description = "處理用戶的增刪查改")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ⚠ 直接回傳 User Entity，JSON 中會包含 password 雜湊等內部欄位，建議改回傳 UserResponse。
     * 另外 "/active" 是固定路徑，Spring 會優先比對它，所以不會被誤認為 "/{id}"。
     */
    @Operation(summary = "查詢所有啟用中的用戶")
    @GetMapping("/active")
    public List<User> getAllActiveUsers() {
        return userService.getAllActiveUsers();
    }

    /** GET /api/users/search?keyword=xxx：{@code @RequestParam} 取得網址 ? 後面的查詢參數。 */
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

    /** 成功回 204；找不到時 Service 丟出例外 → 404。 */
    @Operation(summary = "刪除用戶")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用戶ID") @PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
