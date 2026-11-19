package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtTokenProvider;

/**
 * 認證相關的業務邏輯：登入、註冊、重設密碼、更新 token。
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登入，成功時回傳 JWT。
     *
     * <p>{@code authenticationManager.authenticate()} 內部會：</p>
     * <ol>
     *   <li>呼叫 CustomUserDetailsService.loadUserByUsername() 從資料庫找出用戶。</li>
     *   <li>用 PasswordEncoder 比對密碼雜湊，並檢查帳號是否啟用。</li>
     *   <li>失敗時丟出 AuthenticationException（例如 BadCredentialsException），
     *       由 GlobalExceptionHandler 轉成 401。</li>
     * </ol>
     */
    public String login(AuthRequest request) {
        // 這裡用兩個參數的建構子，代表「尚未驗證」的帳密，交給 AuthenticationManager 驗證
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                ));

        // 驗證成功後，principal 就是資料庫查出來的 User 物件
        return jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal());
    }

    /**
     * 一般使用者註冊：帳號不可重複，預設給予 STAFF 角色。
     */
    public void register(RegisterRequest request) {
        // 帳號重複 → IllegalStateException → GlobalExceptionHandler 轉成 409 Conflict
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("用戶名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(true);

        // 新註冊的用戶預設角色為 STAFF
        Role defaultRole = roleRepository.findByName("STAFF")
                .orElseThrow(() -> new RuntimeException("找不到角色 STAFF"));
        user.setRoles(Set.of(defaultRole));

        userRepository.save(user);
    }

    /**
     * 重設密碼。
     *
     * <p>⚠ 安全性問題：這個 API 在白名單中（不需登入），而且只要知道帳號就能改密碼，
     * 等於任何人都能改掉任何人的密碼。實務上應先驗證身分，例如要求輸入舊密碼，
     * 或寄送一次性的重設連結 / 驗證碼到用戶的 Email。</p>
     */
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 用目前有效的 token 換一張新的 token（延長登入時間）。
     *
     * @param authentication 由 JwtAuthenticationFilter 放進 SecurityContext 的登入者；未登入時為 null
     */
    public String refreshToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("未登入");
        }

        User user = (User) authentication.getPrincipal();
        return jwtTokenProvider.generateToken(user);
    }

    /** 檢查目前的 token 是否有效，並回傳登入者的帳號與權限（前端用來判斷登入狀態）。 */
    public Map<String, Object> checkToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("未登入");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return response;
    }
}
