package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Prompt;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.repository.PromptRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromptService {
    private final PromptRepository promptRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public Prompt handlePrompt(UUID roomId, Prompt prompt) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        prompt.setRoom(room);
        // Simuler réponse IA ici si besoin
        if (prompt.getAiResponse() == null) prompt.setAiResponse("Réponse automatique IA");
        return promptRepository.save(prompt);
    }
}
