package com.example.demo.security;

import com.example.demo.util.DynamicAuthorizationFilter;
import com.example.demo.util.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private DynamicAuthorizationFilter dynamicAuthorizationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // 登入、swagger、健康檢查開放
                 .requestMatchers(
                        "/auth/*.html", 
                        "/api/auth/**", 
                        "/swagger-ui/**", 
                        "/v3/api-docs/**", 
                        "/actuator/**",
                        "/favicon.ico"
                ).permitAll()

                // 靜態資源、網頁 html 開放
                .requestMatchers(
                        "/", 
                        "/static/**", 
                        "/js/**", 
                        "/css/**", 
                        "/*.html",
                        "/user/**",        
                        "/book/**",        
                        "/auth/**",        
                        "/a_office/**",   
                        "/b_office/**",
                        "/c_office/**"
                ).permitAll()


                // 其他都需要登入（授權由 Filter 處理）/api/
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        //  JWT 驗證 Filter（先驗證身份）
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        //  動態授權 Filter（後續比對角色）
        http.addFilterAfter(dynamicAuthorizationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
