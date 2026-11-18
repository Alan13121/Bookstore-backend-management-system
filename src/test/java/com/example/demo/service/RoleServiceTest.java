package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.RoleCreateRequest;
import com.example.demo.dto.RoleResponse;
import com.example.demo.dto.RoleUpdateRequest;
import com.example.demo.exception.ResourceNotFoundException;

@SpringBootTest
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Test
    void getAllRoles() {
        assertNotNull(roleService.getAllRoles());
    }

    @Test
    void getRoleById() {
        assertEquals(1, roleService.getRoleById(1).getId());
    }

    @Test
    @Transactional
    void createRole() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("TEST_ROLE");

        RoleResponse role = roleService.createRole(request);

        assertEquals("TEST_ROLE", role.getName());
    }

    @Test
    @Transactional
    void updateRole() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setName("UPDATED_ROLE");

        RoleResponse role = roleService.updateRole(1, request);

        assertEquals("UPDATED_ROLE", role.getName());
    }

    @Test
    @Transactional
    void deleteRole() {
        roleService.deleteRole(1);

        assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(1));
    }
}
