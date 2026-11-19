package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.Data;

/**
 * 書籍實體，對應資料表 {@code books}。
 *
 * <p>Entity（實體）是「資料表的一列」在 Java 中的樣子，由 JPA / Hibernate 負責
 * 物件與資料表之間的轉換（ORM）。</p>
 *
 * <p>{@code @Data}（Lombok）會在編譯時自動產生 getter、setter、toString、equals、hashCode。</p>
 */
@Data
@Entity
@Table(name = "books")
public class Book {

    /** 主鍵；{@code IDENTITY} 表示交給資料庫的 AUTO_INCREMENT 產生。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 沒有加 {@code @Column} 時，欄位名稱預設與屬性同名。 */
    private String title;

    private String author;

    /** 指定資料庫型別為 text，可存放較長的內容。 */
    @Column(columnDefinition = "text")
    private String description;

    /**
     * 定價。金額使用 {@code BigDecimal} 而非 double，避免浮點數誤差（0.1 + 0.2 ≠ 0.3）。
     * Java 的駝峰命名 listPrice 對應資料表的底線命名 list_price。
     */
    @Column(name = "list_price")
    private BigDecimal listPrice;

    /** 售價。 */
    @Column(name = "sale_price")
    private BigDecimal salePrice;


}
