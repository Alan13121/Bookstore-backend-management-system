package com.example.demo.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResetPasswordRequest", description = "重設密碼請求")
public class ResetPasswordRequest {
    @Schema(description = "用戶名", example = "staff01")
    private String username;

    @Schema(description = "新密碼", example = "newPassword123")
    private String newPassword;
}
