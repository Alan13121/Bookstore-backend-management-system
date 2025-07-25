package com.example.demo.controller;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.UrlRoleMappingService;
import com.example.demo.util.JwtTokenProvider;
import com.example.demo.util.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.MockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean
    private UrlRoleMappingService urlRoleMappingService;
    
    @TestConfiguration
    static class MockConfig {
        @Bean UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }

        @Bean PasswordEncoder passwordEncoder() {
            return Mockito.mock(PasswordEncoder.class);
        }

        @Bean JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }

        @Bean JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean CustomUserDetailsService customUserDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }
    }

    @Test
    void testGetAllUsers() throws Exception {
        User u = new User();
        u.setId(1);
        u.setUsername("test");
        u.setFullName("Test User");
        u.setEmail("test@mail.com");
        u.setPhone("123456");
        u.setEnabled(true);
        Role r = new Role();
        r.setId(1);
        r.setName("ADMIN");
        u.setRoles(Set.of(r));

        when(userRepository.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("test"))
                .andExpect(jsonPath("$[0].fullName").value("Test User"));
    }

    @Test
    void testGetUserById_found() throws Exception {
        User u = new User();
        u.setId(1);
        u.setUsername("test");

        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"));
    }

    @Test
    void testGetUserById_notFound() throws Exception {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveUser() throws Exception {
        String json = """
            {
                "username": "newuser",
                "password": "pass",
                "fullName": "New User",
                "email": "new@mail.com",
                "phone": "987654",
                "enabled": true,
                "roleIds": [1]
            }
            """;

        Role r = new Role();
        r.setId(1);
        r.setName("USER");
        when(roleRepository.findById(1)).thenReturn(Optional.of(r));
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");

        User saved = new User();
        saved.setId(1);
        saved.setUsername("newuser");
        saved.setPassword("encoded-pass");
        saved.setRoles(Set.of(r));

        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void testUpdateUser() throws Exception {
        String json = """
            {
                "username": "updated"
            }
            """;

        User existing = new User();
        existing.setId(1);
        existing.setUsername("old");

        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    void testDeleteUser_found() throws Exception {
        when(userRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUser_notFound() throws Exception {
        when(userRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
