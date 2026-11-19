package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

/**
 * 用戶資料存取層。下面的方法都是「衍生查詢」：Spring Data 依方法名稱自動產生 SQL。
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /** {@code WHERE enabled = true}：查詢所有啟用中的用戶。 */
    List<User> findByEnabledTrue();

    /** {@code WHERE username LIKE %keyword%}：帳號模糊搜尋。 */
    List<User> findByUsernameContaining(String keyword);

    /**
     * ⚠ 目前沒有被使用。Spring Data 會忽略 find 與 By 中間的字（Name），
     * 所以它其實等同 findById，查到的是 User 而不是 String，呼叫時會轉型失敗。之後可以考慮刪除。
     */
    Optional<String> findNameById(Integer id);

    /** {@code WHERE username = ?}：登入時用帳號找人（見 CustomUserDetailsService）。 */
    Optional<User> findByUsername(String username);

    /** {@code SELECT COUNT(*) > 0 ... WHERE username = ?}：註冊時檢查帳號是否重複。 */
    boolean existsByUsername(String username);

}
