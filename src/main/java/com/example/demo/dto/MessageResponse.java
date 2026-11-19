package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MessageResponse", description = "一般訊息回應")
public class MessageResponse {

    @Schema(description = "訊息內容", example = "註冊成功")
    private String message;
}
