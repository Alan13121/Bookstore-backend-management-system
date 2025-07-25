package com.example.demo.controller;

import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(BookControllerTest.MockConfig.class)
@AutoConfigureMockMvc(addFilters = false) // 關掉 security filter
class BookControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired BookRepository bookRepository;

    @MockBean
    private UrlRoleMappingService urlRoleMappingService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        BookRepository bookRepository() {
            return Mockito.mock(BookRepository.class);
        }

        @Bean
        JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }
    }


    @Test
    void testGetAll() throws Exception {
        Book b = new Book();
        b.setId(1);
        b.setTitle("Book Title");
        b.setAuthor("Author");
        b.setDescription("Desc");
        b.setListPrice(BigDecimal.valueOf(100));
        b.setSalePrice(BigDecimal.valueOf(80));

        when(bookRepository.findAll()).thenReturn(List.of(b));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Book Title"));
    }

    @Test
    void testGetOne_found() throws Exception {
        Book b = new Book();
        b.setId(1);
        b.setTitle("Book Title");

        when(bookRepository.findById(1)).thenReturn(Optional.of(b));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book Title"));
    }

    @Test
    void testGetOne_notFound() throws Exception {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        String json = """
            {
                "title": "New Book",
                "author": "Author",
                "description": "Desc",
                "listPrice": 100,
                "salePrice": 80
            }
            """;

        Book b = new Book();
        b.setId(1);
        b.setTitle("New Book");

        when(bookRepository.save(any(Book.class))).thenReturn(b);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Book"));
    }

    @Test
    void testUpdate_found() throws Exception {
        String json = """
            {
                "title": "Updated Title",
                "author": "Author",
                "description": "Desc",
                "listPrice": 120,
                "salePrice": 90
            }
            """;

        Book existing = new Book();
        existing.setId(1);
        existing.setTitle("Old Title");

        when(bookRepository.findById(1)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testUpdate_notFound() throws Exception {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        String json = """
            {
                "title": "Updated Title"
            }
            """;

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_found() throws Exception {
        when(bookRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete_notFound() throws Exception {
        when(bookRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound());
    }
}
