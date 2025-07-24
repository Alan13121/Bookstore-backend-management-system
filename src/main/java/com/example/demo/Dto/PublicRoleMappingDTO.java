package com.example.demo.Dto;

public class PublicRoleMappingDTO {
    private String urlPattern;
    private String roles; // 例如 "ADMIN,STAFF"

    public PublicRoleMappingDTO() {}

    public PublicRoleMappingDTO(String urlPattern, String roles) {
        this.urlPattern = urlPattern;
        this.roles = roles;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
