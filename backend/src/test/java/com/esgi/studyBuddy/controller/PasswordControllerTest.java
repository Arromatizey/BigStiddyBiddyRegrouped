package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.model.PasswordResetToken;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.UserRepository;
import com.esgi.studyBuddy.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PasswordController.class)
public class PasswordControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void forgotPassword_shouldReturnOk_andCreateTokenIfUserExists() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetService.createToken(user)).thenReturn("dummy-token");

        String json = "{\"email\":\"" + email + "\"}";

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userRepository).findByEmail(email);
        verify(passwordResetService).createToken(user);
    }

    @Test
    void forgotPassword_shouldReturnOkEvenIfUserNotFound() throws Exception {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        String json = "{\"email\":\"" + email + "\"}";

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userRepository).findByEmail(email);
        verify(passwordResetService, never()).createToken(any());
    }

    @Test
    void resetPassword_shouldResetPasswordAndDeleteToken() throws Exception {
        String tokenStr = "reset-token";
        String newPassword = "newPass123";

        User user = new User();
        user.setId(UUID.randomUUID());

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenStr);
        token.setUser(user);

        when(passwordResetService.validateToken(tokenStr)).thenReturn(token);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        String json = String.format("{\"token\":\"%s\",\"newPassword\":\"%s\"}", tokenStr, newPassword);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(passwordResetService).validateToken(tokenStr);
        verify(passwordEncoder).encode(newPassword);

        // Capture saved user to verify password was set encoded
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");

        verify(passwordResetService).deleteToken(token);
    }
}

