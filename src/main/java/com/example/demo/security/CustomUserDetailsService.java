package com.example.demo.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;

/**
 * 提供 Spring Security 以用戶名載入用戶。
 *
 * <p>Spring Security 本身不知道用戶存在哪裡（資料庫？LDAP？記憶體？），
 * 所以要實作 {@link UserDetailsService} 告訴它怎麼找。以下兩個地方會呼叫它：</p>
 * <ul>
 *   <li>登入時：AuthenticationManager 用它找出用戶，再比對密碼。</li>
 *   <li>每個帶 JWT 的 request：JwtAuthenticationFilter 用它載入用戶與最新的角色。</li>
 * </ul>
 * <p>因為 {@code User} 已經實作 UserDetails，所以可以直接回傳 User 物件。</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 找不到用戶時必須丟出 UsernameNotFoundException，這是 UserDetailsService 介面的規定。 */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }
}
