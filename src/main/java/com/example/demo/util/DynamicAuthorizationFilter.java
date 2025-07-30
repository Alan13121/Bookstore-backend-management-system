package com.example.demo.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.service.UrlRoleMappingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private UrlRoleMappingService mappingService;

    // 精確比對的白名單 URL
    private static final List<String> WHITELIST = List.of(
        "/favicon.ico",
        "/index.html",
        "/api/auth/check",
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh-token",
        "/api/roles/mappings/public"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 放行白名單 URL 或靜態資源
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 讀取資料庫的動態 URL-Role 對應表
        List<UrlRoleMapping> mappings = mappingService.getAll();

        for (UrlRoleMapping mapping : mappings) {
            String rawPattern = mapping.getUrlPattern(); 
            String[] requiredRoles = mapping.getRoles().split(",");

            if (matchWithExtension(uri, rawPattern)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(r -> r.replace("ROLE_", "")) // 去掉前綴 ROLE_
                        .noneMatch(role -> Arrays.asList(requiredRoles).contains(role))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Forbidden: insufficient role");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response); // 交給下一個 filter 直到執行完
    }

    // 檢查是否為白名單路徑
    private boolean isWhitelisted(String uri) {
        return WHITELIST.stream().anyMatch(uri::equals)
                || uri.startsWith("/static/")
                || uri.endsWith(".html")
                || uri.startsWith("/js/")
                || uri.startsWith("/css/");
    }

    // 支援萬用字元比對與父層資源兼容（如 /api/users/.* 也可涵蓋 /api/users）
    private boolean matchWithExtension(String uri, String rawPattern) {
        String pattern = rawPattern.replace("**", ".*");

        // 完全符合 regex
        if (uri.matches(pattern)) return true;

        // 擴充比對：/api/users/.* 也要能涵蓋 /api/users
        if (pattern.endsWith("/.*")) {
            String basePath = pattern.substring(0, pattern.length() - "/.*".length());
            if (uri.equals(basePath)) return true;
        }

        return false;
    }

}
