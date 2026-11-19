package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void getAllUsers() {
        assertNotNull(userService.getAllUsers());
    }

    @Test
    void searchUsersByUsername() {
        assertEquals("admin", userService.searchUsersByUsername("admin").get(0).getUsername());
    }

    @Test
    void getUserRoleNames() {
        assertNotNull(userService.getUserRoleNames(1));
    }

    @Test
    void getUserById() {
        UserResponse userResponse = userService.getUserById(1);
        assertNotNull(userResponse);
        assertEquals(1, userResponse.getId());
    }

    @Test
    @Transactional
    void createUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("jojo");
        request.setPassword("5555");
        request.setFullName("jojo jo");
        request.setPhone("0977888999");
        request.setEmail("jojo@gmail.com.com");
        request.setEnabled(true);
        request.setRoleIds(Set.of(1, 2));

        UserResponse userResponse = userService.createUser(request);
        assertNotNull(userService.getUserById(userResponse.getId()));
        assertEquals(request.getUsername(), userResponse.getUsername());
        assertEquals(request.getFullName(), userResponse.getFullName());
        assertEquals(request.getPhone(), userResponse.getPhone());
        assertEquals(request.getEmail(), userResponse.getEmail());
        assertEquals(request.getEnabled(), userResponse.getEnabled());
        assertEquals(Set.of("ADMIN", "STAFF"), userResponse.getRoles());
    }

    @Test
    @Transactional
    void updateUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("jojo jo");
        request.setPhone("0977888999");
        request.setEmail("jojo@gmail.com.com");
        request.setEnabled(true);
        request.setRoleIds(Set.of(2, 3));

        UserResponse userResponse = userService.updateUser(1, request);
        assertEquals(request.getFullName(), userResponse.getFullName());
        assertEquals(request.getPhone(), userResponse.getPhone());
        assertEquals(request.getEmail(), userResponse.getEmail());
        assertEquals(request.getEnabled(), userResponse.getEnabled());
        assertEquals(Set.of("STAFF", "WORKER"), userResponse.getRoles());
    }

    @Test
    @Transactional
    void deleteUser() {
        userService.deleteUser(1);

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1));
        assertEquals("找不到用戶，ID: 1", ex.getMessage());
    }

    @Test
    @Transactional
    void createUserWithDuplicateUsername() {
        // data.sql 已有 admin，用同名新增應丟出 DuplicateResourceException（→ 409），
        // 而不是等到 save 時才因 unique constraint 丟 DataIntegrityViolationException（→ 500）
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin");
        request.setPassword("5555");

        DuplicateResourceException ex =
                assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        assertEquals("用戶名已存在", ex.getMessage());
    }

    @Test
    @Transactional
    void updateUserWithDuplicateUsername() {
        // 把用戶 1（admin）改名成已存在的 staff → 應被擋下
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("staff");

        DuplicateResourceException ex =
                assertThrows(DuplicateResourceException.class, () -> userService.updateUser(1, request));
        assertEquals("用戶名已存在", ex.getMessage());
    }

    @Test
    @Transactional
    void updateUserWithSameUsername() {
        // 傳入自己原本的用戶名不算重複，應正常更新
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("admin");

        assertEquals("admin", userService.updateUser(1, request).getUsername());
    }
}
