package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.UrlRoleMapping;

public interface UrlRoleMappingRepository extends JpaRepository<UrlRoleMapping, Long> {
}
