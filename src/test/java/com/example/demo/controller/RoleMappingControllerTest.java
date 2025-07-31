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
class RoleMappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAll() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/roles/mappings");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void getPublicMappings() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/roles/mappings/public");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Transactional
    void createMapping() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/roles/mappings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"urlPattern\":\"/api/test/**\",\"roles\":\"STAFF,ADMIN\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlPattern", equalTo("/api/test/**")))
                .andExpect(jsonPath("$.roles", equalTo("STAFF,ADMIN")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Transactional
    void updateMapping() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/roles/mappings/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"urlPattern\":\"/api/books/**\",\"roles\":\"ADMIN\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlPattern", equalTo("/api/books/**")))
                .andExpect(jsonPath("$.roles", equalTo("ADMIN")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Transactional
    void deleteMapping() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/roles/mappings/{id}", 1);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk()); 
    }
}
