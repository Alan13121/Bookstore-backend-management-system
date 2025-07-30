package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.Dto.AuthRequest;
import com.example.demo.util.JwtTokenProvider;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    public void login(){
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("6969");

        String token = authService.login(request);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(request.getUsername(), jwtTokenProvider.getUsernameFromToken(token));
        
    }

    @Test
    public void register(){

    }

    @Test
    public void resetPassword(){

    }

    @Test
    public void refreshToken(){

    }

    @Test
    public void checkToken(){

    }
}
