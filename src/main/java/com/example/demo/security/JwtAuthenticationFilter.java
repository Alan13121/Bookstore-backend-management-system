package com.example.demo.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * 從 Authorization 標頭解析 JWT，驗證通過後寫入 SecurityContext。
 * 負責回答「這個 request 是誰送來的？」（Authentication，認證）。
 *
 * <ol>
 *   <li>從 Header 取出 {@code Authorization: Bearer <token>}。</li>
 *   <li>驗證 token 的簽名與期限，取出 username。</li>
 *   <li>從資料庫載入用戶（含角色），包成 Authentication 物件放進 SecurityContext。</li>
 *   <li>不論成功與否都繼續往下傳。沒帶 token 或 token 無效就是「未登入」，
 *       要不要擋下來由後面的 DynamicAuthorizationFilter 決定。</li>
 * </ol>
 *
 * <p>{@code OncePerRequestFilter} 保證同一個 request 只執行一次（例如內部 forward 時不會重複執行）。</p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String username = null;

        log.debug("JWT 過濾器處理: {}，Authorization 標頭{}", request.getRequestURI(),
                header == null || header.isBlank() ? "為空" : "存在");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            if (jwtTokenProvider.validateToken(token)) {
                username = jwtTokenProvider.getUsernameFromToken(token);
                log.debug("JWT 有效，用戶: {}", username);
            }
        }

        // token 有效、且這個 request 還沒有設定過登入者時，才建立認證資訊
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 從資料庫重新載入，可以拿到最新的角色與啟用狀態
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 參數依序為：principal（登入者）、credentials（密碼，已驗證過所以給 null）、權限清單
            // 使用三個參數的建構子時，這個物件會被標記為「已驗證」
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // SecurityContextHolder 預設用 ThreadLocal 保存，同一個 request 後續的 Filter、Controller
            // 都拿得到登入者，例如 Controller 方法參數中的 Authentication
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 交給下一個 Filter（一定要呼叫，否則 request 會停在這裡）
        filterChain.doFilter(request, response);
    }
}
