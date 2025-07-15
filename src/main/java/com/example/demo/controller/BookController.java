package com.example.demo.controller;

import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/books")
@Tag(name = "書籍管理", description = "處理書籍的增刪查改")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    @Operation(summary = "查詢所有書")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @Operation(summary = "查詢一本書")
    @GetMapping("/{id}")
    public ResponseEntity<Book> getOne(@PathVariable Integer id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增一本書")
    @PostMapping
    public Book create(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    @Operation(summary = "更新一本書")
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Integer id, @RequestBody Book book) {
        return bookRepository.findById(id).map(b -> {
            b.setTitle(book.getTitle());
            b.setAuthor(book.getAuthor());
            b.setDescription(book.getDescription());
            b.setListPrice(book.getListPrice());
            b.setSalePrice(book.getSalePrice());
            return ResponseEntity.ok(bookRepository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "刪除一本書")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
