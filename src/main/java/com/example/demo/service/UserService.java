package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    // 建構子注入
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByEnabledTrue();
    }

    public List<User> searchUsersByUsername(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    public String getRolesByIdToString(Integer id){
        User user = userRepository.findById(id).orElse(null);
        
        return user.getRoles()
                    .stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(","));
    }
}
