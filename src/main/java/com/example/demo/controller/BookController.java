package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BookCreateRequest;
import com.example.demo.dto.BookResponse;
import com.example.demo.dto.BookUpdateRequest;
import com.example.demo.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/books")
@Tag(name = "書籍管理", description = "書籍的增刪查改")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "查詢所有書籍")
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.getAllBooks();
    }

    @Operation(summary = "查詢單本書籍")
    @GetMapping("/{id}")
    public BookResponse getBookById(@Parameter(description = "書籍 ID") @PathVariable Integer id) {
        return bookService.getBookById(id);
    }

    @Operation(summary = "新增書籍")
    @PostMapping
    public BookResponse createBook(@RequestBody BookCreateRequest request) {
        return bookService.createBook(request);
    }

    @Operation(summary = "更新書籍")
    @PutMapping("/{id}")
    public BookResponse updateBook(@Parameter(description = "書籍 ID") @PathVariable Integer id,
                                   @RequestBody BookUpdateRequest request) {
        return bookService.updateBook(id, request);
    }

    @Operation(summary = "刪除書籍")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@Parameter(description = "書籍 ID") @PathVariable Integer id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
