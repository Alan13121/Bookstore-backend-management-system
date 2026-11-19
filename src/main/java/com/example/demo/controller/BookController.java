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

/**
 * 書籍 API。
 *
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}：
 *       方法的回傳值會自動被 Jackson 轉成 JSON，而不是去找 HTML 頁面。</li>
 *   <li>{@code @RequestMapping("/api/books")}：這個類別所有 API 共同的路徑前綴。</li>
 *   <li>{@code @Tag / @Operation / @Parameter / @SecurityRequirement}：只影響 Swagger 文件顯示，不影響程式行為。</li>
 * </ul>
 *
 * <p>Controller 只負責「接收請求 → 呼叫 Service → 回傳結果」，不寫商業邏輯；
 * 錯誤都交給 Service 丟例外、由 GlobalExceptionHandler 轉成狀態碼，所以這裡看不到 if / try-catch。
 * 權限由 DynamicAuthorizationFilter 控管（預設規則：ADMIN、STAFF 可存取）。</p>
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "書籍管理", description = "書籍的增刪查改")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** GET /api/books：直接回傳物件時，狀態碼預設為 200 OK。 */
    @Operation(summary = "查詢所有書籍")
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.getAllBooks();
    }

    /** GET /api/books/{id}：{@code @PathVariable} 把網址中的 {id} 取出來。 */
    @Operation(summary = "查詢單本書籍")
    @GetMapping("/{id}")
    public BookResponse getBookById(@Parameter(description = "書籍 ID") @PathVariable Integer id) {
        return bookService.getBookById(id);
    }

    /** POST /api/books：{@code @RequestBody} 把 request 的 JSON 轉成 Java 物件。 */
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

    /** DELETE /api/books/{id}：成功回傳 204 No Content（沒有內容），所以用 ResponseEntity 自訂狀態碼。 */
    @Operation(summary = "刪除書籍")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@Parameter(description = "書籍 ID") @PathVariable Integer id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
