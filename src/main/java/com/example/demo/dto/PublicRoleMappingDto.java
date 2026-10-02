package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicRoleMappingDto {
    private String urlPattern;
    private String roles; // 例如 "ADMIN,STAFF"
}
