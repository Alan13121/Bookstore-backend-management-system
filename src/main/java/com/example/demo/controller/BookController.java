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

/**
 * 書籍 API。
 *
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}：
 *       方法的回傳值會自動被 Jackson 轉成 JSON，而不是去找 HTML 頁面。</li>
 *   <li>{@code @RequestMapping("/api/books")}：這個類別所有 API 的共同路徑前綴。</li>
 *   <li>{@code @SecurityRequirement / @Tag / @Operation}：只影響 Swagger 文件的顯示，不影響程式行為。</li>
 * </ul>
 *
 * <p>Controller 只負責「接收請求、呼叫 Service、決定回應狀態碼」，不寫業務邏輯。
 * 權限由 DynamicAuthorizationFilter 控管（預設規則：ADMIN、STAFF 可存取）。</p>
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/books")
@Tag(name = "書籍管理", description = "處理書籍的增刪查改")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** GET /api/books：直接回傳物件時，狀態碼預設為 200 OK。 */
    @Operation(summary = "查詢所有書")
    @GetMapping
    public List<BookDto> getAll() {
        return bookService.getAllBooks();
    }

    /**
     * GET /api/books/{id}：{@code @PathVariable} 把網址中的 {id} 取出來。
     * 用 ResponseEntity 可以自訂狀態碼：找到回 200，找不到回 404。
     */
    @Operation(summary = "查詢一本書")
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getOne(@PathVariable Integer id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/books：{@code @RequestBody} 把 request 的 JSON 轉成 Java 物件。 */
    @Operation(summary = "新增一本書")
    @PostMapping
    public BookDto create(@RequestBody BookCreateRequest request) {
        return bookService.createBook(request);
    }

    /** PUT /api/books/{id}：整筆更新，找不到回 404。 */
    @Operation(summary = "更新一本書")
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> update(
            @PathVariable Integer id,
            @RequestBody BookUpdateRequest request) {
        return bookService.updateBook(id,request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/books/{id}：成功回 204 No Content（沒有回應內容），找不到回 404。 */
    @Operation(summary = "刪除一本書")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
