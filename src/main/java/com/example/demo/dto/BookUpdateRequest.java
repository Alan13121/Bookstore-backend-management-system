package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BookUpdateRequest {
    private String title;
    private String author;
    private String description;
    private BigDecimal listPrice;
    private BigDecimal salePrice;
}
