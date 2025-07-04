package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("user", new User());
        return "form"; // 指向 form.html
    }

    @GetMapping("/user")
    public String userHome() {
        return "redirect:/users";
    }
    
    // 查詢所有用戶
   @GetMapping("/users")
    public String getAllUsers(Model model) {
        System.out.println("正在查詢所有用戶...");
        var users = userRepository.findAll();
        System.out.println("查詢結果數量: " + users.size());
        model.addAttribute("users", users);
        return "users";
    }

    // 根據 ID 查詢單個用戶（顯示編輯頁面）
    @GetMapping("/user/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "user"; // 指向 user.html 作為編輯表單
    }


    @PostMapping("/user")
    public String saveUser(@ModelAttribute User user) {
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        //user.setPassword(passwordEncoder.encode(user.getPassword())); // 加密密碼
        userRepository.save(user);
        System.out.println("保存的用戶: " + user.getUsername());
        return "redirect:/users";
    }

    // 刪除用戶
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return "redirect:/users";
    }

    
}
