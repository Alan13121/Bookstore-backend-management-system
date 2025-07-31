package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dto.CreateUserRequest;
import com.example.demo.Dto.UpdateUserRequest;
import com.example.demo.Dto.UserResponse;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void getAllUsers(){
        assertNotNull(userService.getAllUsers());
    }

    @Test
    public void getUserById(){
        UserResponse userResponse = userService.getUserById(1);
        assertNotNull(userResponse);
        assertEquals(1, userResponse.getId());
    }

    @Test
    @Transactional
    public void createUser(){
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("jojo");
        request.setPassword("5555");
        request.setFullName("jojo jo");
        request.setPhone("0977888999");
        request.setEmail("jojo@gmail.com.com");
        request.setEnabled(true);
        request.setRoleIds(Set.of(1,2));

        UserResponse userResponse = userService.createUser(request);
        assertNotNull(userService.getUserById(userResponse.getId()));
        assertEquals(request.getUsername(), userResponse.getUsername());
        assertEquals(request.getFullName(), userResponse.getFullName());
        assertEquals(request.getPhone(), userResponse.getPhone());
        assertEquals(request.getEmail(), userResponse.getEmail());
        assertEquals(request.getEnabled(), userResponse.getEnabled());
        assertEquals(Set.of("ADMIN","STAFF"), userResponse.getRoles());
    }

    @Test
    @Transactional
    public void updateUser(){
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("jojo jo");
        request.setPhone("0977888999");
        request.setEmail("jojo@gmail.com.com");
        request.setEnabled(true);
        request.setRoleIds(Set.of(2,3));

        UserResponse userResponse = userService.updateUser(1, request);
        assertEquals(request.getFullName(), userResponse.getFullName());
        assertEquals(request.getPhone(), userResponse.getPhone());
        assertEquals(request.getEmail(), userResponse.getEmail());
        assertEquals(request.getEnabled(), userResponse.getEnabled());
        assertEquals(Set.of("STAFF","WORKER"), userResponse.getRoles());
    }

    @Test
    @Transactional
    public void deleteUser(){
        userService.deleteUser(1);
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.deleteUser(1) // 呼叫會丟出例外的方法
        );
        assertEquals("找不到用戶，ID: "+1, ex.getMessage());
    }
}
