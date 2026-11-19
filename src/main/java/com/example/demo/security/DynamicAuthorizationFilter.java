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

/**
 * 依資料庫中的 url_role_mapping 規則，動態檢查目前用戶是否有權存取該 URL。
 * 負責回答「這個人能不能存取這個網址？」（Authorization，授權）。
 *
 * <p>規則範例：</p>
 * <pre>
 *   /api/books/.*  →  ADMIN,STAFF
 *   /api/users/.*  →  ADMIN
 * </pre>
 * <p>網址符合某條規則、但登入者沒有其中任何一個角色時，直接回傳 403。</p>
 *
 * <p>⚠ 目前設計上值得注意的地方（也是之後可以改進的練習題）：</p>
 * <ul>
 *   <li>沒有任何規則符合的網址會直接放行（fail-open），新增 API 時忘了加規則就等於公開。</li>
 *   <li>每個 request 都會查一次資料庫，可以用快取（{@code @Cacheable}）改善。</li>
 *   <li>這裡直接寫出 403，不會經過 GlobalExceptionHandler，所以回應是純文字而不是 JSON。</li>
 * </ul>
 */
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

    /** 後綴符合即放行：HTML 頁面本身公開，頁面裡呼叫的 API 才需要權限。 */
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

        // 逐條檢查：只要有任何一條規則符合網址、但角色不符，就拒絕
        for (UrlRoleMappingResponse mapping : urlRoleMappingService.getAllMappings()) {
            if (matches(uri, mapping.getUrlPattern()) && !hasAnyRole(mapping.getRoles())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("權限不足");
                return; // 不呼叫 filterChain.doFilter，request 就不會到達 Controller
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
        // 取得前一個 Filter（JwtAuthenticationFilter）放進來的登入者；沒登入時為 null
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
