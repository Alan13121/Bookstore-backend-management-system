package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/index")
    public String index() {
        return "index";  
    }
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "歐安崙"); // 把 name 丟進 model
        return "hello"; // 對應到 resources/templates/hello.html
    }
}
