package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 的核心設定。
 *
 * <p>一個 request 進來後會依序經過一串 Filter（過濾器鏈），本專案的重點順序是：</p>
 * <pre>
 *   Request
 *     → JwtAuthenticationFilter     ①「你是誰？」解析 JWT，把登入者放進 SecurityContext
 *     → DynamicAuthorizationFilter  ②「你能做什麼？」依資料庫規則比對角色，不符合就回 403
 *     → ...（Spring Security 其他內建 Filter）
 *     → Controller
 * </pre>
 *
 * <p>{@code @Configuration} 表示這是設定類別，裡面標 {@code @Bean} 的方法回傳值會被註冊到
 * Spring 容器，讓其他類別可以注入使用。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DynamicAuthorizationFilter dynamicAuthorizationFilter;

    /** 建構子注入：Spring 會自動把對應的 Bean 傳進來（只有一個建構子時可以省略 {@code @Autowired}）。 */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          DynamicAuthorizationFilter dynamicAuthorizationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.dynamicAuthorizationFilter = dynamicAuthorizationFilter;
    }

    /**
     * 密碼加密器。
     *
     * <p>BCrypt 每次加密都會加入隨機的鹽（salt），所以同一個密碼每次的雜湊結果都不同，
     * 驗證時要用 {@code passwordEncoder.matches(明碼, 雜湊)}，不能直接比較字串。</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 把 Spring Security 自動建立的 AuthenticationManager 註冊成 Bean，讓 AuthService 可以注入。
     *
     * <p>AuthenticationManager 負責驗證帳密：它會呼叫 {@link CustomUserDetailsService} 從資料庫找出用戶，
     * 再用上面的 PasswordEncoder 比對密碼。</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 無狀態 JWT 設定：內建授權全部放行，
     * 由 {@link JwtAuthenticationFilter} 驗證身分、{@link DynamicAuthorizationFilter} 依資料庫規則授權。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 停用 CSRF：CSRF 攻擊是利用瀏覽器會自動帶 Cookie 的特性；
        // 本專案的 JWT 放在 Authorization Header，不會被自動帶出，所以可以停用
        http.csrf(AbstractHttpConfigurer::disable)
                // 內建授權全部放行，權限判斷都交給 DynamicAuthorizationFilter
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                // 無狀態：伺服器不建立 HttpSession，每個 request 都靠自己帶的 JWT 證明身分
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT Filter 放在內建的帳密登入 Filter 之前，先把身分設定好
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 授權 Filter 必須在 JWT Filter 之後，否則還拿不到登入者的角色
                .addFilterAfter(dynamicAuthorizationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
