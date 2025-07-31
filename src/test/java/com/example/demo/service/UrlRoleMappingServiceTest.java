package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.UrlRoleMapping;

@SpringBootTest
public class UrlRoleMappingServiceTest {

    @Autowired
    private UrlRoleMappingService urlRoleMappingService;

    @Test
    public void getAll() {
        List<UrlRoleMapping> list = urlRoleMappingService.getAll();
        assertNotNull(list);
    }

    @Test
    @Transactional
    public void save() {
        UrlRoleMapping mapping = new UrlRoleMapping();
        mapping.setUrlPattern("/api/test");
        mapping.setRoles("ADMIN,USER");

        UrlRoleMapping saved = urlRoleMappingService.save(mapping);
        assertNotNull(saved.getId());
        assertEquals("/api/test", saved.getUrlPattern());
        assertEquals("ADMIN,USER", saved.getRoles());
    }

    @Test
    @Transactional
    public void delete() {
        assertTrue(urlRoleMappingService.delete(1L));
        assertFalse(urlRoleMappingService.delete(1L));
    }
}
