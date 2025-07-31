package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @Transactional
    public void loadUserByUsername_found() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
    }

    @Test
    @Transactional
    public void loadUserByUsername_notFound() {
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("hehehehe"));
    }
}
