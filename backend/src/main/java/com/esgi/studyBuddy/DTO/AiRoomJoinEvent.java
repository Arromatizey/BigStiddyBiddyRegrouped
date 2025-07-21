package com.esgi.studyBuddy.DTO;

import java.util.UUID;

public record AiRoomJoinEvent(UUID roomId, UUID aiUserId, String topic) {}
