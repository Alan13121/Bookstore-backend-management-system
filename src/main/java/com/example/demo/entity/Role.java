package com.example.demo.entity;

import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 角色實體，對應資料表 {@code roles}（例如 ADMIN、STAFF、WORKER）。
 *
 * <p>與 {@link User} 是多對多關係，中間表為 {@code user_roles}。</p>
 */
@Data
@NoArgsConstructor // JPA 規定 Entity 必須有無參數建構子
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 角色名稱，不含 "ROLE_" 前綴（前綴在 User.getAuthorities() 才加上）。 */
    @Column(name = "name")
    private String name;

    /**
     * 擁有此角色的用戶（多對多的「反向端」）。
     *
     * <ul>
     *   <li>{@code mappedBy = "roles"}：關聯由 User.roles 那一端維護，這裡只是反向查詢用，
     *       中間表的設定寫在 User 上。</li>
     *   <li>{@code LAZY}：真的用到時才查資料庫，避免查角色時把所有用戶也撈出來。</li>
     *   <li>{@code @JsonIgnore}：轉 JSON 時忽略，否則 User → Role → User → … 會無限遞迴。</li>
     *   <li>{@code @ToString.Exclude / @EqualsAndHashCode.Exclude}：同樣是避免雙向關聯造成無限遞迴
     *       （StackOverflowError），也避免觸發 LAZY 載入。</li>
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
