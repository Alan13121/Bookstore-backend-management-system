package com.example.demo.entity;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 用戶實體，對應資料表 {@code users}。
 *
 * <p>這個類別同時實作了 Spring Security 的 {@link UserDetails} 介面，
 * 所以可以直接交給 Spring Security 做登入驗證，不用另外再寫一個轉換類別。
 * Spring Security 會透過以下方法取得它需要的資訊：
 * {@code getUsername()、getPassword()、getAuthorities()、isEnabled()} 等
 * （getUsername / getPassword 由 Lombok {@code @Data} 自動產生）。</p>
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    /** 存的是 BCrypt 雜湊後的密碼，不是明碼。 */
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String email;

    /** 帳號是否啟用；停用的帳號無法登入（見 isEnabled()）。 */
    private Boolean enabled;

    /**
     * 用戶擁有的角色（多對多的「擁有端」，負責維護中間表）。
     *
     * <ul>
     *   <li>{@code @JoinTable}：中間表 user_roles，user_id 指向本表、role_id 指向 roles。</li>
     *   <li>{@code EAGER}：查用戶時一併把角色查出來。因為每次驗證 JWT 都要拿角色判斷權限，
     *       而那時已經不在交易（Transaction）中，若用 LAZY 會發生 LazyInitializationException。</li>
     * </ul>
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    //  實作 UserDetails

    /**
     * 把角色轉成 Spring Security 認得的權限（GrantedAuthority）。
     *
     * <p>Spring Security 的慣例是「角色」要加上 {@code ROLE_} 前綴，例如 ADMIN → ROLE_ADMIN，
     * 這樣 {@code hasRole("ADMIN")} 才能比對成功。</p>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toSet());
    }

    // 以下三個方法本專案沒有使用「帳號過期 / 鎖定 / 密碼過期」功能，所以一律回傳 true

    /** 帳號是否未過期。 */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 帳號是否未被鎖定（例如密碼輸錯太多次可以在這裡鎖帳號）。 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 密碼是否未過期。 */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 帳號是否啟用；回傳 false 時登入會丟出 DisabledException。enabled 為 null 時視為停用。 */
    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
