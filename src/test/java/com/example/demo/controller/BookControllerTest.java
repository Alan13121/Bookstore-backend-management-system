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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "STAFF", roles = {"STAFF"})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAll() throws Exception{
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/books");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getOne() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/books/{id}",1);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    @Transactional
    void create() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/books",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Spring Boot in Action\",\"author\":\"Craig Walls\",\"description\":\"good book\",\"listPrice\":399.00,\"salePrice\":100.00}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Spring Boot in Action")));
    }

    @Test
    @Transactional
    void delete() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/books/{id}",1);
        
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(204));
    }

    @Test
    @Transactional
    void update() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"title\":\"jojo ola ola ola ola\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("jojo ola ola ola ola")));
    }

}

