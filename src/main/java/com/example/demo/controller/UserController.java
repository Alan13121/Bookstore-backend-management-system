package com.example.demo.controller;

import com.example.demo.Dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@Tag(name = "用戶管理", description = "處理用戶的增刪查改")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Operation(summary = "查詢所有用戶")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Operation(summary = "查詢單個用戶")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "用戶ID") @PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toUserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增用戶")
    @PostMapping
    public ResponseEntity<UserResponse> saveUser(
            @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());

        //user.setPassword(request.getPassword());  加密
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = request.getRoleIds().stream()
                    .map(id -> roleRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("角色ID不存在: " + id)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toUserResponse(saved));
    }

    @Operation(summary = "更新用戶")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "用戶ID") @PathVariable Integer id,
            @RequestBody UpdateUserRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFullName(request.getFullName());
                    user.setPhone(request.getPhone());
                    user.setEmail(request.getEmail());
                    user.setEnabled(request.getEnabled());

                    if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                        Set<Role> roles = request.getRoleIds().stream()
                                .map(roleId -> roleRepository.findById(roleId)
                                        .orElseThrow(() -> new IllegalArgumentException("角色ID不存在: " + roleId)))
                                .collect(Collectors.toSet());
                        user.setRoles(roles);
                    }

                    User updated = userRepository.save(user);
                    return ResponseEntity.ok(toUserResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "刪除用戶")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用戶ID") @PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 將 User 實體轉成 UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.getEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}
