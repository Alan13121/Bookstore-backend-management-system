package com.example.demo.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.TokenCheckResponse;
import com.example.demo.dto.TokenResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;

/**
 * 認證相關的商業邏輯：登入、註冊、重設密碼、刷新 token。
 *
 * <p>{@code @Transactional(readOnly = true)} 加在類別上，表示所有方法預設在「唯讀交易」中執行
 * （Hibernate 會省略變更檢查，效能較好）；會寫入資料的方法再個別標上 {@code @Transactional} 覆蓋成可寫入。
 * 同一個交易中的操作要嘛全部成功、要嘛全部回滾（rollback）。</p>
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String DEFAULT_ROLE = "STAFF";

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
     * <p>這裡用兩個參數的建構子建立「尚未驗證」的帳密物件；驗證成功後回傳的 principal 就是資料庫裡的 User。</p>
     */
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        return new TokenResponse(jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal()));
    }

    /** 註冊新用戶，預設給予 STAFF 角色。 */
    @Transactional
    public void register(RegisterRequest request) {
        // 帳號重複 → 409 Conflict
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("用戶名已存在");
        }
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                // 找不到預設角色是系統初始資料有問題，不是使用者的錯，所以用 IllegalStateException → 500
                .orElseThrow(() -> new IllegalStateException("系統缺少預設角色 " + DEFAULT_ROLE));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(true);
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
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用戶不存在"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 用目前有效的 token 換一張新的，延長登入時間。
     *
     * @param authentication JwtAuthenticationFilter 放進 SecurityContext 的登入者；未登入時為 null
     */
    public TokenResponse refreshToken(Authentication authentication) {
        requireAuthenticated(authentication);
        return new TokenResponse(jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal()));
    }

    /** 檢查目前的 token 是否有效，並回傳登入者的帳號與權限（前端用來判斷登入狀態）。 */
    public TokenCheckResponse checkToken(Authentication authentication) {
        requireAuthenticated(authentication);
        return new TokenCheckResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
    }

    /** 未登入時丟出 AuthenticationCredentialsNotFoundException（AuthenticationException 的子類別）→ 401。 */
    private void requireAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("未登入");
        }
    }
}
