package com.example.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;

/**
 * 告訴 Spring Security「怎麼用帳號找到使用者」。
 *
 * <p>Spring Security 本身不知道用戶存在哪裡（資料庫？LDAP？記憶體？），
 * 所以要實作 {@link UserDetailsService} 介面。以下兩個地方會呼叫它：</p>
 * <ul>
 *   <li>登入時：AuthenticationManager 用它找出用戶，再比對密碼。</li>
 *   <li>每個帶 JWT 的 request：JwtAuthenticationFilter 用它載入用戶與角色。</li>
 * </ul>
 * <p>因為 {@code User} 已經實作 UserDetails，所以這裡可以直接回傳 User 物件。</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 用帳號查詢用戶；找不到時必須丟出 UsernameNotFoundException（Spring Security 的規定）。 */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }
}
