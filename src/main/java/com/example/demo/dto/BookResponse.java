package com.example.demo.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 回傳給前端的書籍資料，由 {@code BookService.toResponse()} 從 Entity 轉換而來。
 *
 * <p>不直接回傳 Entity 的好處：資料表結構改變時不會連帶改變 API 格式，
 * 也能決定哪些欄位要對外公開。</p>
 */
@Data
@Schema(name = "BookResponse", description = "書籍資料")
public class BookResponse {

    @Schema(description = "書籍 ID", example = "1")
    private Integer id;

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
