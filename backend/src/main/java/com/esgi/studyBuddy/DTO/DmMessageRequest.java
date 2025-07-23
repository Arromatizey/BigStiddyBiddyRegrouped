package com.esgi.studyBuddy.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class DmMessageRequest {
    private UUID senderId;
    private UUID receiverId;
    private String message;
} 