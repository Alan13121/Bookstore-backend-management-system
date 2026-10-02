package com.example.demo.Dto;

import java.util.Set;

import lombok.Data;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private Boolean enabled;
    private Set<String> roles;
}
