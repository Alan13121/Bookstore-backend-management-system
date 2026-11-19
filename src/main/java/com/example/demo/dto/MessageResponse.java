package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 只帶一段文字訊息的回應，例如「註冊成功」。
 *
 * <p>回傳 JSON 物件而不是純字串，前端就能一律用 {@code res.json()} 解析。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MessageResponse", description = "一般訊息回應")
public class MessageResponse {

    @Schema(description = "訊息內容", example = "註冊成功")
    private String message;
}
