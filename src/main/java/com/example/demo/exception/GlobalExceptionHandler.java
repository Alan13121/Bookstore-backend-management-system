package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.ErrorResponse;

/**
 * 統一處理 Controller 丟出的例外，轉成一致的錯誤回應。
 *
 * <p>{@code @RestControllerAdvice} 會套用到所有 Controller：只要 Controller（或它呼叫的 Service）
 * 丟出例外，Spring 就會找這裡對應型別的 {@code @ExceptionHandler} 方法處理，
 * 所以 Controller 裡不需要到處寫 try-catch。</p>
 *
 * <p>本專案的約定：Service 用不同的例外型別表達不同錯誤，這裡再轉成對應的 HTTP 狀態碼。</p>
 *
 * <p>⚠ 注意：Filter 在 Controller 之前執行，Filter 裡發生的錯誤（例如 403 權限不足）
 * 不會經過這裡。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 找不到資料（Service 以 IllegalArgumentException 表示）→ 404。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** 資料狀態衝突，例如用戶名已存在 → 409。 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    /** 登入失敗或未登入 → 401。例如 AuthenticationManager 驗證帳密失敗時丟出的 BadCredentialsException。 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤，或尚未登入");
    }

    /** 組出 {@code {"status": ..., "message": ...}} 格式的回應。 */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
