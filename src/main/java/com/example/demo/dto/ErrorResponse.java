package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "錯誤回應")
public class ErrorResponse {
    @Schema(description = "HTTP 狀態碼", example = "404")
    private int status;

    @Schema(description = "錯誤訊息", example = "找不到用戶，ID: 99")
    private String message;
}
