package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * 書籍，對應資料表 {@code books}。
 *
 * <p>Entity 是「資料表的一列」在 Java 中的樣子，由 JPA（實作為 Hibernate）負責物件與資料表之間的轉換（ORM）。
 * {@code @Data}（Lombok）會在編譯時自動產生 getter、setter、toString、equals、hashCode。</p>
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

    /** 指定資料庫型別為 text，可以存放較長的內容。 */
    @Column(columnDefinition = "text")
    private String description;

    /**
     * 定價。
     *
     * <p>金額用 {@code BigDecimal} 而不是 double，避免浮點數誤差（0.1 + 0.2 ≠ 0.3）。
     * {@code @Column(name)} 把 Java 的駝峰命名對應到資料表的底線命名。</p>
     */
    @Column(name = "list_price")
    private BigDecimal listPrice;

    /** 售價。 */
    @Column(name = "sale_price")
    private BigDecimal salePrice;
}
