package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 統一處理 Controller 丟出的例外，轉成一致的 {@link ErrorResponse}。
 *
 * <p>{@code @RestControllerAdvice} 會套用到所有 Controller：只要 Controller（或它呼叫的 Service）
 * 丟出例外，Spring 就會找這裡型別相符的 {@code @ExceptionHandler} 方法處理，
 * 所以 Controller 裡不需要到處寫 try-catch。</p>
 *
 * <p>⚠ Filter 在 Controller 之前執行，Filter 裡發生的錯誤（例如 DynamicAuthorizationFilter 的 403）
 * 不會經過這裡。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 查無資料 → 404。 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** 資料重複，例如用戶名已存在 → 409 Conflict。 */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    /** 請求參數不合法 → 400。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * 登入失敗或未登入 → 401。
     * 例如帳密錯誤時 AuthenticationManager 丟出的 BadCredentialsException 就是它的子類別。
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤，或尚未登入");
    }

    /** 系統狀態異常，例如缺少必要的初始資料 → 500。這是伺服器的問題，所以額外寫一筆 error log。 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        log.error("系統狀態異常", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /** 組出 {@code {"status": ..., "message": ...}} 格式的回應。 */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
