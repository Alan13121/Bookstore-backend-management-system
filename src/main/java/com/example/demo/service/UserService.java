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

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            user.setRoles(findRoles(request.getRoleIds()));
        }
        return toResponse(userRepository.save(user));
    }

    /** 只更新非 null 的欄位。 */
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
