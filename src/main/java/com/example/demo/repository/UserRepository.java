package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByEnabledTrue();
    List<User> findByUsernameContaining(String keyword);
    Optional<String> findNameById(Integer id);
    
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

}
