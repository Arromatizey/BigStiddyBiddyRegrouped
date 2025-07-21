package com.esgi.studyBuddy.kafka;

import com.esgi.studyBuddy.DTO.AiResponseEvent;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.RoomMessageRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiResponseListener {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMessageRepository roomMessageRepository;
    private final ObjectMapper objectMapper; // Injected for JSON deserialization

    private static final String AI_USER_EMAIL = "ai@studybuddy.com";

    @KafkaListener(topics = "ai-response-events", groupId = "studybuddy-group")
    public void handleAiResponse(String message) {
        try {
            AiResponseEvent event = objectMapper.readValue(message, AiResponseEvent.class);
            log.info("Received AI response for roomId {}: {}", event.roomId(), event.response());

            Room room = roomRepository.findById(event.roomId()).orElseThrow();
            User aiUser = userRepository.findByEmail(AI_USER_EMAIL)
                    .orElseGet(() -> {
                        User user = new User();
                        user.setEmail(AI_USER_EMAIL);
                        user.setDisplayName("StudyBuddy AI");
                        user.setPassword("fake");
                        user.setVerified(true);
                        return userRepository.save(user);
                    });

            RoomMessage aiMessage = RoomMessage.builder()
                    .room(room)
                    .user(aiUser)
                    .message(event.response())
                    .build();

            roomMessageRepository.save(aiMessage);
            log.info("Saved AI message to DB.");
        } catch (Exception e) {
            log.error("Failed to process AI response message: {}", message, e);
        }
    }
}
