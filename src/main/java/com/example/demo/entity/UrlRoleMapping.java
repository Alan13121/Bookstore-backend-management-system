package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * 動態權限規則，對應資料表 {@code url_role_mapping}。
 *
 * <p>每一筆代表「符合 urlPattern 的網址，只有 roles 裡的角色可以存取」，
 * 由 {@code DynamicAuthorizationFilter} 在每個 request 進來時比對。</p>
 *
 * <p>把規則放在資料庫而不是寫死在程式裡（例如 {@code @PreAuthorize}），
 * 管理員就能在執行中直接修改權限，不需要重新部署。</p>
 */
@Data
@Entity
@Table(name = "url_role_mapping")
public class UrlRoleMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL 比對規則（regex），例如 {@code /api/books/.*}。對應欄位 url_pattern（Spring Boot 預設會把駝峰轉成底線）。 */
    private String urlPattern;

    /** 允許的角色，逗號分隔，例如 {@code ADMIN,STAFF}。 */
    private String roles;
}
