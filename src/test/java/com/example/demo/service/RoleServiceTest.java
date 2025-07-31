package com.example.demo.service;

import com.example.demo.Dto.RoleCreateRequest;
import com.example.demo.Dto.RoleDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Test
    public void getAllRoles() {
        List<RoleDto> list = roleService.getAllRoles();
        assertNotNull(list);
    }

    @Test
    public void getRoleById() {
        Optional<RoleDto> role = roleService.getRoleById(1);
        assertNotNull(role);
        if (role.isPresent()) {
            assertEquals(1, role.get().getId());
        }
    }

    @Test
    @Transactional
    public void createRole() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("TEST_ROLE");

        RoleDto dto = roleService.createRole(request);
        assertEquals("TEST_ROLE", dto.getName());
    }

    @Test
    @Transactional
    public void updateRole() {
        RoleCreateRequest updateReq = new RoleCreateRequest();
        updateReq.setName("UPDATED_ROLE");

        RoleDto updated = roleService.updateRole(1, updateReq);
        assertEquals("UPDATED_ROLE", updated.getName());
    }

    @Test
    @Transactional
    public void deleteRole() {
        assertTrue(roleService.deleteRole(1));
        assertFalse(roleService.deleteRole(1));
    }
}
