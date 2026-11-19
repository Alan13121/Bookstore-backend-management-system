package com.example.demo.util;

import java.io.IOException;
import java.util.Arrays;
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

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.service.UrlRoleMappingService;

import lombok.extern.slf4j.Slf4j;

/**
 * 動態授權過濾器：負責回答「這個人能不能存取這個網址？」（Authorization 授權）。
 *
 * <p>規則存在資料表 {@code url_role_mapping}，例如：</p>
 * <pre>
 *   /api/books/.*  →  ADMIN,STAFF
 *   /api/users/.*  →  ADMIN
 * </pre>
 * <p>當 request 的網址符合某條規則，但登入者沒有其中任何一個角色時，直接回傳 403。</p>
 *
 * <p>⚠ 目前的設計值得注意的地方（也是之後可以改進的練習題）：</p>
 * <ul>
 *   <li>沒有任何規則符合的網址會直接放行（fail-open），新增 API 時若忘了加規則就等於公開。</li>
 *   <li>每個 request 都會查一次資料庫，可以用快取（@Cacheable）改善。</li>
 *   <li>這裡直接寫 403 回應，不會經過 GlobalExceptionHandler，所以格式是純文字而非 JSON。</li>
 * </ul>
 */
@Slf4j
@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    private final UrlRoleMappingService mappingService;

    public DynamicAuthorizationFilter(UrlRoleMappingService mappingService) {
        this.mappingService = mappingService;
    }

    // 白名單 URL：完全相同才放行
    private static final List<String> WHITELIST = List.of(
        "/api/roles/mappings/public",
        "/favicon.ico"
    );

    // 開頭符合就放行：靜態資源、登入註冊相關 API、Swagger 文件、Actuator 監控端點
    private static final List<String> STARTS_WITH = List.of(
        "/static/", "/js/", "/css/", "/api/auth/",
        "/v3/api-docs/", "/actuator/", "/swagger-ui/"
    );

    // 結尾符合就放行：所有 HTML 頁面（頁面本身公開，頁面裡呼叫的 API 才需要權限）
    private static final List<String> ENDS_WITH = List.of(
        ".html"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        log.debug("動態授權檢查: {}", uri);
        // 在白名單的都不用比對 直接批准
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response); // 交給下個filter
            return;
        }

        // 讀取資料庫的動態 url_Role 對應表
        List<UrlRoleMapping> mappings = mappingService.getAll();

        // 逐條檢查規則：只要有任何一條規則符合網址、但角色不符，就拒絕
        for (UrlRoleMapping mapping : mappings) {
            String rawPattern = mapping.getUrlPattern();
            String[] requiredRoles = mapping.getRoles().split(","); // "ADMIN,STAFF" → ["ADMIN", "STAFF"]

            if (matchWithExtension(uri, rawPattern)) {
                // 取得前一個 Filter（JwtAuthenticationFilter）放進來的登入者；沒登入時為 null
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                // 未登入，或登入者的所有角色都不在允許清單中 → 403 Forbidden
                if (auth == null || auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(r -> r.replace("ROLE_", "")) // 去掉前綴 ROLE_
                        .noneMatch(role -> Arrays.asList(requiredRoles).contains(role))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("權限不足");
                    return; // 不呼叫 filterChain.doFilter，request 就不會到達 Controller
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
