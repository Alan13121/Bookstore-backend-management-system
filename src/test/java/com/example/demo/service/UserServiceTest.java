package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testGetAllActiveUsers() {
        // Arrange
        User u1 = new User(); u1.setUsername("user1");
        User u2 = new User(); u2.setUsername("user2");
        when(userRepository.findByEnabledTrue()).thenReturn(List.of(u1, u2));

        // Act
        List<User> result = userService.getAllActiveUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository, times(1)).findByEnabledTrue();
    }

    @Test
    void testSearchUsersByUsername() {
        // Arrange
        String keyword = "admin";
        User u1 = new User(); u1.setUsername("admin1");
        when(userRepository.findByUsernameContaining(keyword)).thenReturn(List.of(u1));

        // Act
        List<User> result = userService.searchUsersByUsername(keyword);

        // Assert
        assertEquals(1, result.size());
        assertEquals("admin1", result.get(0).getUsername());
        verify(userRepository, times(1)).findByUsernameContaining(keyword);
    }

    @Test
    void testGetAllActiveUsersReturnsEmptyList() {
        // Arrange
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());

        // Act
        List<User> result = userService.getAllActiveUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEnabledTrue();
    }

    @Test
    void testSearchUsersByUsernameReturnsEmptyList() {
        // Arrange
        String keyword = "notfound";
        when(userRepository.findByUsernameContaining(keyword)).thenReturn(List.of());

        // Act
        List<User> result = userService.searchUsersByUsername(keyword);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByUsernameContaining(keyword);
    }
}
