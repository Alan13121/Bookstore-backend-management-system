package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Swagger（OpenAPI）的安全設定。
 *
 * <p>定義一個名為 {@code bearerAuth} 的驗證方式：在 HTTP Header 帶上
 * {@code Authorization: Bearer <JWT>}。</p>
 *
 * <p>Controller 上標註 {@code @SecurityRequirement(name = "bearerAuth")} 之後，
 * Swagger UI 右上角會出現「Authorize」按鈕，貼上登入取得的 token 就能直接測試需要登入的 API。</p>
 *
 * <p>這個類別本身沒有程式碼，只是用來掛註解；{@code @Configuration} 讓 Spring 掃描到它。</p>
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
