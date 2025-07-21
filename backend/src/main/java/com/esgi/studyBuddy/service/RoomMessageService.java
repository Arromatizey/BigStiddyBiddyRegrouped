package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.DTO.AiMessageEvent;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.RoomMessageRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomMessageService {
    private final RoomMessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, AiMessageEvent> kafkaTemplate;

    public List<RoomMessage> getLastMessages(UUID roomId) {
        return messageRepository.findTop50ByRoom_IdOrderByCreatedAtDesc(roomId);
    }

    public void saveMessage(UUID roomId, UUID userId, String content) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        RoomMessage message = RoomMessage.builder()
                .room(room)
                .user(user)
                .message(content)
                .build();

        messageRepository.save(message);
    }
    public void saveMessageAndNotifyAI(UUID roomId, UUID userId, String content) {
        User user = userRepository.findById(userId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        // Save message to DB
        RoomMessage message = RoomMessage.builder()
                .user(user)
                .room(room)
                .message(content)
                .build();
        messageRepository.save(message);

        // Publish Kafka event
        AiMessageEvent event = new AiMessageEvent(roomId, userId, content);
        kafkaTemplate.send("ai-message-events", event);
        log.info("Sent message to AI for processing: {}", event);
    }
}
