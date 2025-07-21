package com.esgi.studyBuddy.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ResetPasswordRequest {
    private String token;
    private String newPassword;

    // Getters and setters
}
