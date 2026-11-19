package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.UrlRoleMapping;

/** 動態權限規則的資料存取層。 */
public interface UrlRoleMappingRepository extends JpaRepository<UrlRoleMapping, Long> {
}
