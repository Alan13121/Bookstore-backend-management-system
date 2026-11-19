package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

/** 用戶的資料存取層。以下都是「衍生查詢」：Spring Data 依方法名稱自動產生 SQL。 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /** {@code WHERE username = ?}：登入時用帳號找人（見 CustomUserDetailsService）。 */
    Optional<User> findByUsername(String username);

    /** {@code SELECT COUNT(*) > 0 ... WHERE username = ?}：註冊時檢查帳號是否重複。 */
    boolean existsByUsername(String username);

    /** {@code WHERE enabled = true}：查詢所有啟用中的用戶。 */
    List<User> findByEnabledTrue();

    /** {@code WHERE username LIKE %keyword%}：帳號模糊搜尋。 */
    List<User> findByUsernameContaining(String keyword);
}
