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

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "ADMIN", roles = {"ADMIN"})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllActiveUsers() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/users/active");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchUsers() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/users/search")
                .param("keyword", "admin");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRolesByIdToString() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/users/{id}/roles", 1);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/users");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getUserById() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/users/{id}", 1);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    @Transactional
    void createUser() throws Exception {
        String jsonBody = "{ \"username\": \"newuser\", \"password\": \"123456\", \"fullName\": \"New User\", \"email\": \"new@example.com\", \"phone\": \"0911222333\" }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo("newuser")));
    }

    @Test
    @Transactional
    void updateUser() throws Exception {
        String jsonBody = "{ \"fullName\": \"Updated Name\", \"email\": \"update@example.com\", \"phone\": \"0999888777\" }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/users/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", equalTo("Updated Name")));
    }

    @Test 
    @Transactional
    void updateUser_notFound() throws Exception {
        String jsonBody = "{ \"fullName\": \"No User\", \"email\": \"nouser@example.com\", \"phone\": \"0000000000\" }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/users/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void deleteUser() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/users/{id}", 1);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().is(204));
    }
}
