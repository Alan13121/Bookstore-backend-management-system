package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.BookCreateRequest;
import com.example.demo.dto.BookResponse;
import com.example.demo.dto.BookUpdateRequest;
import com.example.demo.exception.ResourceNotFoundException;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    void getAllBooks() {
        assertNotNull(bookService.getAllBooks());
    }

    @Test
    void getBookById() {
        assertEquals(1, bookService.getBookById(1).getId());
    }

    @Test
    void getBookByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(9999));
    }

    @Test
    @Transactional
    void createBook() {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("jojo");
        request.setAuthor("荒木老頭");
        request.setDescription("歐拉");
        request.setSalePrice(new BigDecimal("888"));
        request.setListPrice(new BigDecimal("777"));

        BookResponse book = bookService.createBook(request);

        assertNotNull(bookService.getBookById(book.getId()));
        assertEquals("jojo", book.getTitle());
        assertEquals("荒木老頭", book.getAuthor());
        assertEquals("歐拉", book.getDescription());
        assertEquals(0, book.getSalePrice().compareTo(new BigDecimal("888")));
        assertEquals(0, book.getListPrice().compareTo(new BigDecimal("777")));
    }

    @Test
    @Transactional
    void updateBook() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("jojo");
        request.setAuthor("荒木老頭");
        request.setDescription("歐拉");
        request.setSalePrice(new BigDecimal("888"));
        request.setListPrice(new BigDecimal("777"));

        BookResponse book = bookService.updateBook(1, request);

        assertEquals("jojo", book.getTitle());
        assertEquals("荒木老頭", book.getAuthor());
        assertEquals("歐拉", book.getDescription());
        assertEquals(0, book.getSalePrice().compareTo(new BigDecimal("888")));
        assertEquals(0, book.getListPrice().compareTo(new BigDecimal("777")));
    }

    @Test
    @Transactional
    void deleteBook() {
        bookService.deleteBook(1);

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(1));
        assertEquals("找不到書籍，ID: 1", ex.getMessage());
    }
}
