package com.example.demo.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 更新書籍請求 DTO（PUT /api/books/{id}）。
 * <p>要更新哪一本書由網址上的 id 決定，所以 body 不需要 id。
 * 注意目前是「整筆覆蓋」：沒傳的欄位會被設成 null。</p>
 */
@Data
@Schema(name = "BookUpdateRequest", description = "更新書籍請求")
public class BookUpdateRequest {
    @Schema(description = "書名", example = "Spring Boot in Action")
    private String title;
    @Schema(description = "作者", example = "Craig Walls")
    private String author;
    @Schema(description = "簡述", example = "good book")
    private String description;
    @Schema(description = "定價", example = "399")
    private BigDecimal listPrice;
    @Schema(description = "售價", example = "100")
    private BigDecimal salePrice;
}
