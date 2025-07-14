package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/admin/hello")
    public String adminHello() {
        return "Hello Admin!";
    }

    @GetMapping("/staff/hello")
    public String staffHello() {
        return "Hello Staff or Admin!";
    }
}
