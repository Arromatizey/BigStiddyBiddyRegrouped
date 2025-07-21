package com.esgi.studyBuddy.DTO;

import java.util.UUID;

public record AiResponseEvent(
        UUID roomId,
        String response
) {}
