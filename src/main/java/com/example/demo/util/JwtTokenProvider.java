package com.example.demo.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT（JSON Web Token）工具類別：負責產生、驗證、解析 token。
 *
 * <p>JWT 由三段組成，用「.」隔開：{@code Header.Payload.Signature}</p>
 * <ul>
 *   <li>Header：使用的演算法（這裡是 HS512）。</li>
 *   <li>Payload：存放的資料（claims），例如 username、角色、過期時間。
 *       ⚠ 只有 Base64 編碼、沒有加密，任何人都能解開來看，所以不能放密碼等機密。</li>
 *   <li>Signature：用伺服器的密鑰對前兩段簽名。只要內容被竄改，簽名就驗證不過，
 *       這就是 JWT 防偽造的原理。</li>
 * </ul>
 *
 * <p>{@code @Component} 讓 Spring 把它註冊成 Bean（預設是單例 Singleton），其他類別可以注入。
 * {@code @Slf4j}（Lombok）會自動產生 {@code log} 物件用來寫日誌。</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** 簽名用的密鑰，從 application.properties 的 jwt.secret 讀取（{@code @Value} 注入設定值）。 */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** token 有效時間（毫秒）。冒號後面是預設值：設定檔沒寫時用 86400000 ms = 1 天。 */
    @Value("${jwt.expiration-ms:86400000}") // 預設 1 天
    private Long jwtExpirationMs;

    /**
     * 產生 JWT。
     *
     * @param userDetails 已通過驗證的使用者
     * @return 簽好名的 token 字串，例如 {@code eyJhbGciOi...}
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())   // sub：這張 token 屬於誰
                .claim("roles", roles)                // 自訂欄位：角色清單
                .issuedAt(now)                        // iat：簽發時間
                .expiration(expiryDate)               // exp：過期時間
                .signWith(getSigningKey(), Jwts.SIG.HS512) // 用密鑰簽名
                .compact();                           // 組成最終字串
    }

    /**
     * 驗證 Token 是否正確 & 沒過期。
     *
     * <p>jjwt 在解析時會一併檢查簽名與過期時間，失敗就丟例外，
     * 所以「能成功解析」就代表 token 合法。</p>
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // 能成功解析就算合法
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 驗證失敗: {}", e.getMessage());
            return false;
        }
    }

    /** 從 Token 取得 username（Payload 中的 sub 欄位）。 */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * 從 Token 取得角色。
     *
     * <p>目前專案沒有用到：JwtAuthenticationFilter 會重新從資料庫載入角色，
     * 好處是角色被修改後能立即生效，代價是每個 request 都多查一次資料庫。</p>
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("roles", List.class);
    }

    /** 驗證簽名並解析 Token，回傳 Payload（Claims）；驗證失敗會丟出 JwtException。 */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 獲取簽名用 SecretKey。
     *
     * <p>HS512 演算法規定密鑰至少 512 bits（64 bytes），太短的密鑰容易被暴力破解。
     * ⚠ 正式環境的密鑰不應寫在設定檔並 commit 進 git，應改用環境變數注入。</p>
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            throw new IllegalArgumentException("JWT secret key must be at least 512 bits (64 bytes) for HS512.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
