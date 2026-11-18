package com.example.demo.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.dto.UrlRoleMappingResponse;
import com.example.demo.service.UrlRoleMappingService;

import lombok.extern.slf4j.Slf4j;

/** 依資料庫中的 url_role_mapping 規則，動態檢查目前用戶是否有權存取該 URL。 */
@Slf4j
@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    /** 完全符合即放行。 */
    private static final List<String> WHITELIST = List.of(
            "/api/roles/mappings/public",
            "/favicon.ico");

    /** 前綴符合即放行（靜態資源、登入相關、API 文件）。 */
    private static final List<String> WHITELIST_PREFIXES = List.of(
            "/static/", "/js/", "/css/", "/api/auth/",
            "/v3/api-docs/", "/actuator/", "/swagger-ui/");

    /** 後綴符合即放行。 */
    private static final List<String> WHITELIST_SUFFIXES = List.of(".html");

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String WILDCARD_SUFFIX = "/.*";

    private final UrlRoleMappingService urlRoleMappingService;

    public DynamicAuthorizationFilter(UrlRoleMappingService urlRoleMappingService) {
        this.urlRoleMappingService = urlRoleMappingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        log.debug("動態授權檢查: {}", uri);

        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        for (UrlRoleMappingResponse mapping : urlRoleMappingService.getAllMappings()) {
            if (matches(uri, mapping.getUrlPattern()) && !hasAnyRole(mapping.getRoles())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("權限不足");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String uri) {
        return WHITELIST.stream().anyMatch(uri::equals)
                || WHITELIST_PREFIXES.stream().anyMatch(uri::startsWith)
                || WHITELIST_SUFFIXES.stream().anyMatch(uri::endsWith);
    }

    /** 目前用戶是否具備 roles（逗號分隔，不含 ROLE_ 前綴）中的任一角色。 */
    private boolean hasAnyRole(String roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        List<String> requiredRoles = List.of(roles.split(","));
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace(ROLE_PREFIX, ""))
                .anyMatch(requiredRoles::contains);
    }

    /** 以 regex 比對；{@code **} 視為 {@code .*}，且 {@code /api/users/.*} 也涵蓋 {@code /api/users}。 */
    private boolean matches(String uri, String rawPattern) {
        String pattern = rawPattern.replace("**", ".*");
        if (uri.matches(pattern)) {
            return true;
        }
        if (pattern.endsWith(WILDCARD_SUFFIX)) {
            String basePath = pattern.substring(0, pattern.length() - WILDCARD_SUFFIX.length());
            return uri.equals(basePath);
        }
        return false;
    }
}
