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
}
