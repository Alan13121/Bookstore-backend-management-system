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

    // 白名單 URL
    private static final List<String> WHITELIST = List.of(
        "/api/roles/mappings/public",
        "/favicon.ico"
    );
    
    private static final List<String> STARTS_WITH = List.of(
        "/static/", "/js/", "/css/", "/api/auth/", 
        "/v3/api-docs/", "/actuator/", "/swagger-ui/"
    );

    private static final List<String> ENDS_WITH = List.of(
        ".html"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        
        System.out.println("[DEBUG] DynamicAuthorizationFilter triggered ");
        // 在白名單的都不用比對 直接批准
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);//交給下個filter
            return;
        }

        // 讀取資料庫的動態 url_Role 對應表
        List<UrlRoleMapping> mappings = mappingService.getAll();

        for (UrlRoleMapping mapping : mappings) {
            String rawPattern = mapping.getUrlPattern(); 
            String[] requiredRoles = mapping.getRoles().split(",");

            if (matchWithExtension(uri, rawPattern)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .peek(r -> System.out.println("[DEBUG] before replace: " + r))  // debug 印出 r
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

    // 檢查是否為白名單路徑 靜態資源以及 登入需求 直接開放
    private boolean isWhitelisted(String uri) {
        return WHITELIST.stream().anyMatch(uri::equals)
                || STARTS_WITH.stream().anyMatch(uri::startsWith)
                || ENDS_WITH.stream().anyMatch(uri::endsWith);
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
