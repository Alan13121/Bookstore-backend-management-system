package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @Transactional
    void loadUserByUsernameFound() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
    }

    @Test
    @Transactional
    void loadUserByUsernameNotFound() {
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("hehehehe"));
    }
}
