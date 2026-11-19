package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 動態權限規則，對應資料表 {@code url_role_mapping}。
 *
 * <p>每一筆代表：「符合 urlPattern 的網址，只有 roles 裡列出的角色可以存取」。
 * 由 {@code DynamicAuthorizationFilter} 在每個 request 進來時比對。</p>
 *
 * <p>把規則放在資料庫而不是寫死在程式（例如 {@code @PreAuthorize}）的好處是：
 * 管理員可以在執行期間直接修改權限，不需要重新部署。</p>
 */
@Entity
@Table(name = "url_role_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlRoleMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 網址的正規表示式（regex），對應欄位 url_pattern（Spring Boot 預設會把駝峰轉成底線）。 */
    private String urlPattern;  // e.g., /api/books/.*

    /** 允許的角色，用逗號分隔，不含 "ROLE_" 前綴。 */
    private String roles;       // e.g., ADMIN,STAFF
}
