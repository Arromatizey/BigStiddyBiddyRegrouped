package com.esgi.studyBuddy.DTO;

import java.util.List;
import java.util.UUID;

public record AiMessageEvent(
        UUID roomId,
        UUID userId,
        String message,
        List<String> context // 👈 contexte conversationnel ajouté
) {}
