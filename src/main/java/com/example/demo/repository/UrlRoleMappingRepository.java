package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UrlRoleMapping;

/**
 * URL 與角色對應規則的資料存取層，提供 {@code DynamicAuthorizationFilter} 讀取權限規則。
 *
 * <p>{@code @Repository} 在繼承 JpaRepository 時其實可以省略（Spring Data 會自動註冊），
 * 加上它的額外作用是把資料庫例外轉成 Spring 統一的 {@code DataAccessException}。</p>
 */
@Repository
public interface UrlRoleMappingRepository extends JpaRepository<UrlRoleMapping, Long> {
}
