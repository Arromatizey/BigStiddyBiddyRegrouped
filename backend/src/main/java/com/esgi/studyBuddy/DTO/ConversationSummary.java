package com.esgi.studyBuddy.DTO;

import lombok.Data;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConversationSummary {
    private UUID otherUserId;
    private String otherUserName;
    private String lastMessage;
    private Instant lastMessageTime;
    private int unreadCount;
} 