package com.esgi.studyBuddy.DTO;

import lombok.Data;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DmMessageResponse {
    private UUID id;
    private UUID senderId;
    private String senderName;
    private UUID receiverId;
    private String receiverName;
    private String message;
    private Instant createdAt;
} 