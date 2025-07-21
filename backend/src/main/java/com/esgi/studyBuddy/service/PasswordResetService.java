package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.PasswordResetToken;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;

    public PasswordResetService(PasswordResetTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public String createToken(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        tokenRepo.save(resetToken);
        return token;
    }

    public PasswordResetToken validateToken(String token) {
        return tokenRepo.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
    }

    public void deleteToken(PasswordResetToken token) {
        tokenRepo.delete(token);
    }
}
