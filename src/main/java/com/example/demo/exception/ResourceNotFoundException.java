package com.example.demo.exception;

/** 查無指定資源，對應 HTTP 404。 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
