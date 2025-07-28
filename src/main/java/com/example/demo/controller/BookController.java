package com.example.demo.controller;

import com.example.demo.Dto.BookCreateRequest;
import com.example.demo.Dto.BookDto;
import com.example.demo.Dto.BookUpdateRequest;
import com.example.demo.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/books")
@Tag(name = "書籍管理", description = "處理書籍的增刪查改")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "查詢所有書")
    public List<BookDto> getAll() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查詢一本書")
    public ResponseEntity<BookDto> getOne(@PathVariable Integer id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "新增一本書")
    public BookDto create(@RequestBody BookCreateRequest request) {
        return bookService.createBook(request);
    }

    @PutMapping
    @Operation(summary = "更新一本書")
    public ResponseEntity<BookDto> update(@RequestBody BookUpdateRequest request) {
        return bookService.updateBook(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除一本書")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
