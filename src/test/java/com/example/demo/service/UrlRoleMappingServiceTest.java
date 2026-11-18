package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UrlRoleMappingCreateRequest;
import com.example.demo.dto.UrlRoleMappingResponse;
import com.example.demo.dto.UrlRoleMappingUpdateRequest;
import com.example.demo.exception.ResourceNotFoundException;

@SpringBootTest
class UrlRoleMappingServiceTest {

    @Autowired
    private UrlRoleMappingService urlRoleMappingService;

    @Test
    void getAllMappings() {
        assertFalse(urlRoleMappingService.getAllMappings().isEmpty());
    }

    @Test
    void getPublicMappings() {
        assertFalse(urlRoleMappingService.getPublicMappings().isEmpty());
    }

    @Test
    @Transactional
    void createMapping() {
        UrlRoleMappingCreateRequest request = new UrlRoleMappingCreateRequest();
        request.setUrlPattern("/api/test");
        request.setRoles("ADMIN,USER");

        UrlRoleMappingResponse mapping = urlRoleMappingService.createMapping(request);

        assertNotNull(mapping.getId());
        assertEquals("/api/test", mapping.getUrlPattern());
        assertEquals("ADMIN,USER", mapping.getRoles());
    }

    @Test
    @Transactional
    void updateMapping() {
        UrlRoleMappingUpdateRequest request = new UrlRoleMappingUpdateRequest();
        request.setUrlPattern("/api/books/**");
        request.setRoles("ADMIN");

        UrlRoleMappingResponse mapping = urlRoleMappingService.updateMapping(1L, request);

        assertEquals(1L, mapping.getId());
        assertEquals("ADMIN", mapping.getRoles());
    }

    @Test
    @Transactional
    void updateMappingNotFound() {
        UrlRoleMappingUpdateRequest request = new UrlRoleMappingUpdateRequest();
        request.setUrlPattern("/x");
        request.setRoles("ADMIN");

        assertThrows(ResourceNotFoundException.class, () -> urlRoleMappingService.updateMapping(9999L, request));
    }

    @Test
    @Transactional
    void deleteMapping() {
        urlRoleMappingService.deleteMapping(1L);

        assertThrows(ResourceNotFoundException.class, () -> urlRoleMappingService.deleteMapping(1L));
    }
}
