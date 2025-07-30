package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllRoles() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/roles");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getRoleById() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/roles/{id}", 1);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    @Transactional
    void createRole() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"C-MANAGER\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("C-MANAGER")));
    }

    @Test
    @Transactional
    void updateRole() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/roles/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"MANAGER\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("MANAGER")));
    }

    @Test
    @Transactional
    void deleteRole() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/roles/{id}", 1);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(204));
    }
}
