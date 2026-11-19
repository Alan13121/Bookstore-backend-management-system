package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一的錯誤回應格式，由 {@code GlobalExceptionHandler} 產生。
 *
 * <p>例如：{@code {"status": 404, "message": "找不到書籍，ID: 99"}}。
 * 前端只要讀 message 就能顯示錯誤，不用針對每支 API 寫不同的解析方式。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "錯誤回應")
public class ErrorResponse {

    @Schema(description = "HTTP 狀態碼", example = "404")
    private int status;

    @Schema(description = "錯誤訊息", example = "找不到用戶，ID: 99")
    private String message;
}
