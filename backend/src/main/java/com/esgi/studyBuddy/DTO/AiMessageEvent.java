package com.esgi.studyBuddy.DTO;

import java.util.UUID;

public record AiMessageEvent(UUID roomId, UUID userId, String message) {}
