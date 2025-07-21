package com.esgi.studyBuddy.DTO;

import java.util.UUID;

public record PostRoomMessageRequest(UUID userId, String message) {}
