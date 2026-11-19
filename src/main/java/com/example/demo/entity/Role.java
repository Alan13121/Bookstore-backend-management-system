package com.example.demo.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 角色，對應資料表 {@code roles}（例如 ADMIN、STAFF、WORKER）。
 *
 * <p>與 {@link User} 是多對多關係，中間表為 {@code user_roles}。
 * {@code @NoArgsConstructor} 是必要的：JPA 規定 Entity 一定要有無參數建構子。</p>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 角色名稱，不含 {@code ROLE_} 前綴（前綴在 {@code User.getAuthorities()} 才加上）。 */
    private String name;

    /**
     * 擁有這個角色的用戶，是多對多關係中的「反向端」。
     *
     * <ul>
     *   <li>{@code mappedBy = "roles"}：關聯由 {@code User.roles} 那一端維護，中間表的設定寫在 User 上，
     *       這裡只是讓我們可以從角色反查用戶。</li>
     *   <li>{@code LAZY}：真的用到時才查資料庫，避免查角色時把所有用戶一起撈出來。</li>
     *   <li>{@code @JsonIgnore}：轉 JSON 時忽略，否則 User → Role → User → … 會無限遞迴。</li>
     *   <li>{@code @ToString.Exclude / @EqualsAndHashCode.Exclude}：同樣是為了避免雙向關聯互相呼叫
     *       造成 StackOverflowError，也避免不小心觸發 LAZY 載入。</li>
     * </ul>
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users;

    public Role(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
