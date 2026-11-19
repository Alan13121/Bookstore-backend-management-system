package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Role;

/** 角色資料存取層。 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * 衍生查詢（Derived Query）：Spring Data 會解析方法名稱自動產生 SQL，
     * 相當於 {@code SELECT * FROM roles WHERE name = ?}。
     *
     * <p>回傳 {@code Optional}，強迫呼叫端處理「查不到」的情況，避免 NullPointerException。</p>
     */
    Optional<Role> findByName(String name);
}
