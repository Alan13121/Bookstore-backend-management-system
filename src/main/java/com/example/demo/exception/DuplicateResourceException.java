package com.example.demo.exception;

/** 資源已存在（例如用戶名重複），對應 HTTP 409。 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
