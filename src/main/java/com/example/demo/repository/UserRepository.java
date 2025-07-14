package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByEnabledTrue();
    List<User> findByUsernameContaining(String keyword);
    Optional<String> findNameById(Integer id);
    Optional<User> findById(Integer id);
    
    @Query("SELECT u.roles FROM User u WHERE u.username = :username")
    Set<Role> findRolesByUsername(@Param("username") String username);
    Optional<User> findByUsername(String username);

}
