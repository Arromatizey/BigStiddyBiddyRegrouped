package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserById_shouldReturnUser_ifExists() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_ifNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(userId);
        });

        assertEquals("User not found", thrown.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void getAllUsers_shouldReturnList() {
        List<User> users = List.of(new User(), new User());

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void createUser_shouldSaveAndReturnId() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        when(userRepository.save(user)).thenReturn(user);

        UUID resultId = userService.createUser(user);

        assertEquals(userId, resultId);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_shouldCallDeleteById() {
        UUID userId = UUID.randomUUID();

        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }
}
