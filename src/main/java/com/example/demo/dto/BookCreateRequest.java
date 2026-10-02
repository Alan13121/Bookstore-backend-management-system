package com.example.demo.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(name = "BookCreateRequest", description = "新增書籍請求")
public class BookCreateRequest {
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
