package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "books")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String author;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "list_price")
    private BigDecimal listPrice;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}

