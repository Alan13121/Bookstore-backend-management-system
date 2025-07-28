package com.example.demo.service;

import com.example.demo.Dto.BookCreateRequest;
import com.example.demo.Dto.BookDto;
import com.example.demo.Dto.BookUpdateRequest;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository; // 模擬 Repository

    @InjectMocks
    private BookService bookService; // 測試目標

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1);
        book.setTitle("Spring Boot 入門");
        book.setAuthor("John");
    }

    @Test
    void getAllBooks_shouldReturnListOfBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookDto> result = bookService.getAllBooks();

        assertEquals(1, result.size());
        assertEquals("Spring Boot 入門", result.get(0).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void getBookById_shouldReturnBookDtoIfFound() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Optional<BookDto> result = bookService.getBookById(1);

        assertTrue(result.isPresent());
        assertEquals("Spring Boot 入門", result.get().getTitle());
        verify(bookRepository).findById(1);
    }

    @Test
    void getBookById_shouldReturnEmptyIfNotFound() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        Optional<BookDto> result = bookService.getBookById(1);

        assertTrue(result.isEmpty());
        verify(bookRepository).findById(1);
    }

    @Test
    void createBook_shouldSaveAndReturnBookDto() {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Spring Boot 入門");
        request.setAuthor("John");

        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto result = bookService.createBook(request);

        assertNotNull(result);
        assertEquals("Spring Boot 入門", result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateAndReturnBookDtoIfFound() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setId(1);
        request.setTitle("Updated Title");
        request.setAuthor("John");

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Optional<BookDto> result = bookService.updateBook(request);

        assertTrue(result.isPresent());
        verify(bookRepository).findById(1);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_shouldReturnEmptyIfBookNotFound() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setId(1);
        request.setTitle("Updated Title");
        request.setAuthor("John");

        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        Optional<BookDto> result = bookService.updateBook(request);

        assertTrue(result.isEmpty());
        verify(bookRepository).findById(1);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_shouldDeleteAndReturnTrueIfExists() {
        when(bookRepository.existsById(1)).thenReturn(true);

        boolean result = bookService.deleteBook(1);

        assertTrue(result);
        verify(bookRepository).existsById(1);
        verify(bookRepository).deleteById(1);
    }

    @Test
    void deleteBook_shouldReturnFalseIfNotExists() {
        when(bookRepository.existsById(1)).thenReturn(false);

        boolean result = bookService.deleteBook(1);

        assertFalse(result);
        verify(bookRepository).existsById(1);
        verify(bookRepository, never()).deleteById(1);
    }
}
