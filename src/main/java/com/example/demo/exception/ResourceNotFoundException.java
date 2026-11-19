package com.example.demo.exception;

/**
 * 查無指定資源，對應 HTTP 404。
 *
 * <p>繼承 {@code RuntimeException}（非受檢例外），所以方法上不用宣告 {@code throws}，
 * 呼叫端也不用 try-catch，交給 GlobalExceptionHandler 統一處理即可。</p>
 *
 * <p>自訂例外的好處：用「型別」表達錯誤的種類，比起都丟 RuntimeException 再比對訊息文字清楚得多。</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
