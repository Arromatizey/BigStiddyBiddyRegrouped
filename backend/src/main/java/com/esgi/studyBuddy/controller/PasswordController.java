package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.DTO.ResetPasswordRequest;
import com.esgi.studyBuddy.model.PasswordResetToken;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.UserRepository;
import com.esgi.studyBuddy.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordController {

    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(UserRepository userRepository,
                              PasswordResetService passwordResetService,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = passwordResetService.createToken(user);
            // Send email logic goes here (SMTP or API)
            System.out.println("Reset link: http://localhost:4200/reset-password?token=" + token);
        });
        return ResponseEntity.ok("If the email exists, a reset link was sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        PasswordResetToken token = passwordResetService.validateToken(req.getToken());
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        passwordResetService.deleteToken(token);
        return ResponseEntity.ok("Password successfully reset.");
    }
}
