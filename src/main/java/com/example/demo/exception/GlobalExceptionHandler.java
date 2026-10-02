package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.ErrorResponse;

/** 統一處理 Controller 丟出的例外，轉成一致的錯誤回應。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 找不到資料（Service 以 IllegalArgumentException 表示）。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** 資料狀態衝突，例如用戶名已存在。 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    /** 登入失敗或未登入。 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤，或尚未登入");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
