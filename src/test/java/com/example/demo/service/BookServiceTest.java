package com.example.demo.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.BookCreateRequest;
import com.example.demo.dto.BookDto;
import com.example.demo.dto.BookUpdateRequest;

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
        Optional<BookDto> book = bookService.getBookById(1);
        assertNotNull(book);
        BookDto b = book.get();
        assertEquals(1, b.getId());
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

        BookDto b = bookService.createBook(request);
        assertNotNull(bookService.getBookById(b.getId()));
        assertEquals("jojo", b.getTitle());
        assertEquals("荒木老頭", b.getAuthor());
        assertEquals("歐拉", b.getDescription());
        assertEquals(0, b.getSalePrice().compareTo(new BigDecimal("888")));
        assertEquals(0, b.getListPrice().compareTo(new BigDecimal("777")));
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
        BookDto b = bookService.updateBook(1,request).get();

        assertNotNull(bookService.getBookById(b.getId()));
        assertEquals("jojo", b.getTitle());
        assertEquals("荒木老頭", b.getAuthor());
        assertEquals("歐拉", b.getDescription());
        assertEquals(0, b.getSalePrice().compareTo(new BigDecimal("888")));
        assertEquals(0, b.getListPrice().compareTo(new BigDecimal("777")));

    }

    @Test
    @Transactional
    void deleteBook() {
        assertTrue(bookService.deleteBook(1));
        assertFalse(bookService.deleteBook(1));
    }

}
