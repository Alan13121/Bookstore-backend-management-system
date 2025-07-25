package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "RegisterRequest", description = "註冊新用戶請求")
public class RegisterRequest {
    @Schema(description = "用戶名", example = "staff01")
    private String username;

    @Schema(description = "密碼", example = "123456")
    private String password;

    @Schema(description = "姓名", example = "ANDY NOODLE")
    private String fullName;

    @Schema(description = "Email", example = "staff01@example.com")
    private String email;

    @Schema(description = "電話", example = "0912345678")
    private String phone;
}
