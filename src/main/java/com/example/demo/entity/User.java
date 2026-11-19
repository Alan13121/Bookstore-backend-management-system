package com.example.demo.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 系統用戶，對應資料表 {@code users}，同時作為 Spring Security 的 {@link UserDetails}。
 *
 * <p>實作 UserDetails 之後，這個物件可以直接交給 Spring Security 做登入驗證，
 * 不用再另外寫轉換類別。Spring Security 會透過 {@code getUsername()、getPassword()、
 * getAuthorities()、isEnabled()} 等方法取得它需要的資訊（getUsername / getPassword 由 Lombok 產生）。</p>
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    /** BCrypt 雜湊後的密碼，不是明碼。 */
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String email;

    /** 帳號是否啟用；停用的帳號無法登入（見 {@link #isEnabled()}）。 */
    private Boolean enabled;

    /**
     * 用戶擁有的角色，是多對多關係中的「擁有端」，負責維護中間表。
     *
     * <ul>
     *   <li>{@code @JoinTable}：中間表 user_roles，user_id 指向本表、role_id 指向 roles。</li>
     *   <li>{@code EAGER}：查用戶時一併查出角色。因為 JwtAuthenticationFilter 每次都要拿角色判斷權限，
     *       而那時已經不在交易（Transaction）中，若用 LAZY 會發生 LazyInitializationException。</li>
     * </ul>
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    /**
     * 把角色轉成 Spring Security 認得的權限（GrantedAuthority）。
     *
     * <p>Spring Security 的慣例是角色要加上 {@code ROLE_} 前綴，例如 ADMIN → ROLE_ADMIN，
     * 這樣 {@code hasRole("ADMIN")} 才能比對成功。</p>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
    }

    // 以下三個方法對應「帳號過期 / 帳號鎖定 / 密碼過期」，本專案沒有使用這些功能，所以一律回傳 true。
    // 例如之後想做「密碼輸錯 5 次鎖帳號」，就可以改寫 isAccountNonLocked()。
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

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
