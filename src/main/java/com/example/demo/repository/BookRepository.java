package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Book;

/**
 * 書籍資料存取層。
 *
 * <p>只要繼承 {@code JpaRepository<實體類別, 主鍵型別>}，Spring Data JPA 會在啟動時
 * 自動產生實作類別，不用自己寫 SQL 就能使用：
 * {@code findAll()、findById()、save()、deleteById()、existsById()、count()} 等方法。</p>
 *
 * <p>{@code save()} 會依照 id 判斷：id 為 null → INSERT；id 有值 → UPDATE。</p>
 */
public interface BookRepository extends JpaRepository<Book, Integer> {
}
