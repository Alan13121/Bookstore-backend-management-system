package com.example.demo.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

/**
 * 用戶管理的業務邏輯層（給管理員使用；一般註冊流程在 AuthService）。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 建構子注入
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 查詢所有啟用的用戶
    // ⚠ 直接回傳 Entity，JSON 會包含 password 雜湊，建議改成回傳 UserResponse
    public List<User> getAllActiveUsers() {
        return userRepository.findByEnabledTrue();
    }

    // 依使用者名稱模糊查詢（同樣直接回傳 Entity，有相同問題）
    public List<User> searchUsersByUsername(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    // 取得使用者的角色(字串)
    public String getRolesByIdToString(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶，ID: " + id));
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));
    }

    /** 查詢所有用戶（含停用），轉成不含密碼的 UserResponse。 */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    /** 依 id 查詢用戶；找不到丟 IllegalArgumentException → 404。 */
    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶，ID: " + id));
        return toUserResponse(user);
    }

    /** 管理員新增用戶，可直接指定角色與啟用狀態。 */
    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        // 密碼一定要先加密再存，資料庫中只保存 BCrypt 雜湊值
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        // 沒有指定時預設為啟用
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        // 把角色 id 清單轉成 Role 物件；任何一個 id 不存在就整筆失敗
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = request.getRoleIds().stream()
                    .map(id -> roleRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("角色ID不存在: " + id)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return toUserResponse(userRepository.save(user));
    }

    /**
     * 更新用戶（部分更新）：只有 request 中不是 null 的欄位才會被修改。
     * 角色清單若有傳入，會整組取代原本的角色。
     */
    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶，ID: " + id));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = request.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new IllegalArgumentException("角色ID不存在: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return toUserResponse(userRepository.save(user));
    }

    /** 刪除用戶；找不到丟 IllegalArgumentException → 404。 */
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("找不到用戶，ID: " + id);
        }
        userRepository.deleteById(id);
    }

    /** Entity → DTO：刻意不複製 password，角色只取名稱。 */
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
