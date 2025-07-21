package com.esgi.studyBuddy.DTO;


import lombok.Data;

@Data
public class UserUpdateRequest {
    private String displayName;
    private String avatarUrl;
    private String email;
}


