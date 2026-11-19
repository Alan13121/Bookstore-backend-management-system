package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "url_role_mapping")
public class UrlRoleMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL 比對規則（regex），例如 {@code /api/books/.*}。 */
    private String urlPattern;

    /** 允許的角色，逗號分隔，例如 {@code ADMIN,STAFF}。 */
    private String roles;
}
