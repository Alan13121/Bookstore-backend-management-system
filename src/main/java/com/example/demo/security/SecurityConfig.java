package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.util.DynamicAuthorizationFilter;
import com.example.demo.util.JwtAuthenticationFilter;

/**
 * Spring Security 的核心設定。
 *
 * <p>一個 HTTP request 進來後，會依序經過一串 Filter（過濾器鏈），本專案的重點順序是：</p>
 * <pre>
 *   Request
 *     → JwtAuthenticationFilter     ①「你是誰？」解析 JWT，把登入者放進 SecurityContext
 *     → DynamicAuthorizationFilter  ②「你能做什麼？」依資料庫規則比對角色，不符合就回 403
 *     → ...（Spring Security 其他內建 Filter）
 *     → Controller
 * </pre>
 *
 * <p>{@code @Configuration} 表示這是設定類別，裡面標 {@code @Bean} 的方法回傳值會被
 * 註冊到 Spring 容器中，讓其他類別可以注入使用。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final DynamicAuthorizationFilter dynamicAuthorizationFilter;

    /** 建構子注入：Spring 會自動把對應的 Bean 傳進來（只有一個建構子時可省略 @Autowired）。 */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, DynamicAuthorizationFilter dynamicAuthorizationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.dynamicAuthorizationFilter = dynamicAuthorizationFilter;
    }

    /**
     * 密碼加密器。BCrypt 每次加密都會加入隨機鹽（salt），所以同一個密碼每次雜湊結果都不同，
     * 驗證時要用 {@code passwordEncoder.matches(明碼, 雜湊)} 而不是直接比較字串。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 把 Spring Security 自動建立的 AuthenticationManager 註冊成 Bean，讓 AuthService 可以注入。
     *
     * <p>AuthenticationManager 負責「驗證帳密」：它會呼叫 {@code CustomUserDetailsService}
     * 從資料庫找出用戶，再用上面的 PasswordEncoder 比對密碼。</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /** 設定過濾器鏈（Security Filter Chain）。 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // 停用CSRF：CSRF 攻擊是利用瀏覽器自動帶 Cookie 的特性；
            // 本專案的 JWT 是放在 Authorization Header，不會被自動帶出，所以可以停用
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // 這裡不用內建的過濾器 直接都交給 dynamicAuthorizationFilter 處理規則
                .anyRequest().permitAll()
            )
            // 無狀態：伺服器不建立 HttpSession，每個 request 都靠自己帶的 JWT 證明身分
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        //  JWT 驗證 Filter（先驗證身份）
        //  放在內建的帳密登入 Filter 之前，讓帶 JWT 的 request 先完成身分設定
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        //  動態授權 Filter（後續比對角色）
        //  必須在 JWT Filter 之後，否則還拿不到登入者的角色
        http.addFilterAfter(dynamicAuthorizationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
