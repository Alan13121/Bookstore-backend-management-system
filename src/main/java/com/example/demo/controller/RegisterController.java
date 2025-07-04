package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.User;

@Controller
public class RegisterController {

    

    // @PostMapping("/register")
    // public String submitForm(@ModelAttribute User user) {
    //     System.out.println("收到使用者：" + user.getUsername() + ", " + user.getEmail());
    //     return "收到使用者：" + user.getUsername() + ", " + user.getEmail(); // 可以顯示提交結果
    // }
    @PostMapping("/register")
    public String submitForm(@ModelAttribute User user, Model model) {
        System.out.println("收到使用者："+ user.getId()  + ", " + user.getUsername() + ", " + user.getPassword());
        model.addAttribute("id", user.getId());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("password", user.getPassword());
        return "result"; // 指向 result.html
    }
}
