package com.example.demo.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(name = "BookDto", description = "書籍資料")
public class BookDto {
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
