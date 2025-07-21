package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.PasswordResetToken;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepo;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createToken_shouldGenerateAndSaveToken() {
        User user = new User();
        user.setId(java.util.UUID.randomUUID());

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);

        String token = passwordResetService.createToken(user);

        assertNotNull(token);
        verify(tokenRepo).save(captor.capture());

        PasswordResetToken savedToken = captor.getValue();
        assertEquals(token, savedToken.getToken());
        assertEquals(user, savedToken.getUser());
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void validateToken_shouldReturnTokenIfValid() {
        String tokenStr = "valid-token";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenStr);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepo.findByToken(tokenStr)).thenReturn(Optional.of(token));

        PasswordResetToken result = passwordResetService.validateToken(tokenStr);

        assertEquals(token, result);
    }

    @Test
    void validateToken_shouldThrowIfTokenNotFound() {
        String tokenStr = "missing-token";
        when(tokenRepo.findByToken(tokenStr)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> passwordResetService.validateToken(tokenStr));

        assertEquals("Invalid or expired token", ex.getMessage());
    }

    @Test
    void validateToken_shouldThrowIfTokenExpired() {
        String tokenStr = "expired-token";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenStr);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        when(tokenRepo.findByToken(tokenStr)).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> passwordResetService.validateToken(tokenStr));

        assertEquals("Invalid or expired token", ex.getMessage());
    }

    @Test
    void deleteToken_shouldCallRepositoryDelete() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("some-token");

        passwordResetService.deleteToken(token);

        verify(tokenRepo).delete(token);
    }
}
