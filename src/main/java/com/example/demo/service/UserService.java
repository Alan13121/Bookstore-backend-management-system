package com.example.demo.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

/**
 * 用戶管理的商業邏輯（給管理員使用；自行註冊的流程在 AuthService）。
 *
 * <p>{@code @Transactional(readOnly = true)} 加在類別上，表示所有方法預設在「唯讀交易」中執行
 * （Hibernate 會省略變更檢查，效能較好）；會寫入資料的方法再個別標上 {@code @Transactional} 覆蓋成可寫入。
 * 同一個交易中的操作要嘛全部成功、要嘛全部回滾（rollback）。</p>
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return toResponses(userRepository.findAll());
    }

    public List<UserResponse> getAllActiveUsers() {
        return toResponses(userRepository.findByEnabledTrue());
    }

    public List<UserResponse> searchUsersByUsername(String keyword) {
        return toResponses(userRepository.findByUsernameContaining(keyword));
    }

    public UserResponse getUserById(Integer id) {
        return toResponse(findUser(id));
    }

    /** 取得用戶的角色名稱，以逗號分隔。 */
    public String getUserRoleNames(Integer id) {
        return findUser(id).getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));
    }

    /** 管理員新增用戶，可以直接指定角色與是否啟用。 */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        // 密碼一定要先加密再存，資料庫只保存 BCrypt 雜湊值
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        // 沒有指定時預設為啟用
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            user.setRoles(findRoles(request.getRoleIds()));
        }
        return toResponse(userRepository.save(user));
    }

    /** 部分更新：只更新非 null 的欄位；有傳角色時會整組取代原本的角色。 */
    @Transactional
    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
        User user = findUser(id);
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            user.setRoles(findRoles(request.getRoleIds()));
        }
        return toResponse(userRepository.save(user));
    }

    /** 刪除用戶；找不到時丟出 404。 */
    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw notFound(id);
        }
        userRepository.deleteById(id);
    }

    private User findUser(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    /** 把角色 id 轉成 Role 物件；任何一個 id 不存在就丟出 404，整筆操作失敗。 */
    private Set<Role> findRoles(Set<Integer> roleIds) {
        return roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("找不到角色，ID: " + roleId)))
                .collect(Collectors.toSet());
    }

    private ResourceNotFoundException notFound(Integer id) {
        return new ResourceNotFoundException("找不到用戶，ID: " + id);
    }

    private List<UserResponse> toResponses(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    /** Entity → DTO：刻意不複製 password，角色只取名稱。 */
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setEnabled(user.getEnabled());
        response.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return response;
    }
}
