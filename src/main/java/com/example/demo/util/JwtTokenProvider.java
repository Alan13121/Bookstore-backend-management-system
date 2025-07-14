package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:my-secret-key-change-me}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}") // 預設 1 天
    private Long jwtExpirationMs;

    /**
     * 產生 JWT
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 驗證 Token 是否正確 & 沒過期
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()  // ✅ 0.12.x 正確寫法
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT: " + e.getMessage());
        }
        return false;
    }

    /**
     * 取得 username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 取得角色 (可選)
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("roles", List.class);
    }

    /**
     * 取得簽名密鑰
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) { // 小於 512 bits
            throw new IllegalArgumentException("JWT secret key must be at least 512 bits (64 bytes) for HS512.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
