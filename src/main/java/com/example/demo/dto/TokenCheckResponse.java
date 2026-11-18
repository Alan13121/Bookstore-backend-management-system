package com.example.demo.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TokenCheckResponse", description = "登入狀態檢查結果")
public class TokenCheckResponse {

    @Schema(description = "用戶名", example = "admin")
    private String username;

    @Schema(description = "權限清單", example = "[\"ROLE_ADMIN\"]")
    private List<String> authorities;
}
