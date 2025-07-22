package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.DTO.AiMessageEvent;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.RoomMessageRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

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

        RoomMessage savedMessage = messageRepository.save(message);
        
        // 🔥 Diffuser le message via WebSocket en temps réel
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/messages", savedMessage);
        log.info("📡 Message diffusé via WebSocket pour la room {}: {}", roomId, content);
    }
    public void saveMessageAndNotifyAI(UUID roomId, UUID userId, String content) {
        User user = userRepository.findById(userId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        // 📝 Save new message to DB
        RoomMessage message = RoomMessage.builder()
                .user(user)
                .room(room)
                .message(content)
                .build();
        RoomMessage savedMessage = messageRepository.save(message);
        
        // 🔥 Diffuser le message AI via WebSocket en temps réel
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/messages", savedMessage);
        log.info("📡 Message AI diffusé via WebSocket pour la room {}: {}", roomId, content);

        // 📜 Get conversation context
        List<RoomMessage> previousMessages = messageRepository.findTop50ByRoom_IdOrderByCreatedAtDesc(roomId);
        List<String> context = previousMessages.stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt())) // Optional: restore chronological order
                .map(m -> m.getUser().getDisplayName() + ": " + m.getMessage())
                .toList();

        // 📤 Publish Kafka event
        AiMessageEvent event = new AiMessageEvent(roomId, userId, content, context);
        kafkaTemplate.send("ai-message-events", event);
        log.info("Sent message to AI with context: {}", event);
    }
    @Transactional
    public void deleteMessagesByRoomId(UUID roomId) {
        messageRepository.deleteByRoom_Id(roomId);
    }

}
