package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.BookCreateRequest;
import com.example.demo.dto.BookDto;
import com.example.demo.dto.BookUpdateRequest;
import com.example.demo.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/books")
@Tag(name = "書籍管理", description = "處理書籍的增刪查改")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "查詢所有書")
    @GetMapping
    public List<BookDto> getAll() {
        return bookService.getAllBooks();
    }

    @Operation(summary = "查詢一本書")
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getOne(@PathVariable Integer id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增一本書")
    @PostMapping
    public BookDto create(@RequestBody BookCreateRequest request) {
        return bookService.createBook(request);
    }

    @Operation(summary = "更新一本書")
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> update(
            @PathVariable Integer id,
            @RequestBody BookUpdateRequest request) {
        return bookService.updateBook(id,request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "刪除一本書")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
