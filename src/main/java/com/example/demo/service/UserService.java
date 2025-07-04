package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllActiveUsers() {
        return userRepository.findByEnabledTrue();
    }

    public List<User> searchUsersByUsername(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

}