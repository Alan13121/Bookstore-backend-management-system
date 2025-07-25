package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AuthRequest", description = "登入請求")
public class AuthRequest {
    @Schema(description = "用戶名", example = "admin")
    private String username;

    @Schema(description = "密碼", example = "123456")
    private String password;
}
