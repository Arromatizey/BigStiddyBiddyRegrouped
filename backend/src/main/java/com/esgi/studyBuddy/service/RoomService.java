package com.esgi.studyBuddy.service;

import ch.qos.logback.classic.Logger;
import com.esgi.studyBuddy.DTO.RoomDurationUpdateRequest;
import com.esgi.studyBuddy.model.*;
import com.esgi.studyBuddy.repository.RoomMemberRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.esgi.studyBuddy.DTO.AiRoomJoinEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final KafkaTemplate<String, AiRoomJoinEvent> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String AI_EMAIL = "ai@studybuddy.com";

    @Transactional
    public UUID createRoom(Room room) {
        // 1. Save the room
        Room saved = roomRepository.save(room);

        // 2. Add the room owner as a member
        RoomMember ownerMember = RoomMember.builder()
                .room(saved)
                .user(saved.getOwner())
                .userId(saved.getOwner().getId())
                .roomId(saved.getId())
                .role(UserRole.owner)
                .build();
        roomMemberRepository.save(ownerMember);

        // 3. Get or create an AI user
        User aiUser = userRepository.findByEmail(AI_EMAIL).orElseGet(() -> {
            User ai = new User();
            ai.setEmail(AI_EMAIL);
            ai.setDisplayName("StudyBuddy AI");
            ai.setVerified(true);
            ai.setPassword("dummy-password"); // never used
            return userRepository.save(ai);
        });

        // 4. Add an AI user as a member
        RoomMember aiMember = RoomMember.builder()
                .room(saved)
                .user(aiUser)
                .userId(aiUser.getId())
                .roomId(saved.getId())
                .role(UserRole.member)
                .build();
        roomMemberRepository.save(aiMember);

        // 5. Send Kafka event to AI microservice
        AiRoomJoinEvent event = new AiRoomJoinEvent(saved.getId(), aiUser.getId(), saved.getTopic());
        kafkaTemplate.send("ai-room-events", event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully to topic {} with offset {}",
                                "ai-room-events", result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message to Kafka: {}", ex.getMessage());
                    }
                });
        log.info("Sent AI join event for room {}", saved.getId());


        return saved.getId();
    }

    @Transactional
    public void joinRoom(UUID roomId, UUID userId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        RoomMemberId id = new RoomMemberId(userId, roomId);
        if (roomMemberRepository.existsById(id)) return;
        roomMemberRepository.save(RoomMember.builder()
                .room(room)
                .user(user)
                .userId(userId)
                .roomId(roomId)
                .role(UserRole.member)
                .build());
    }

    @Transactional
    public void updateTheme(UUID roomId, String themeConfig) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setThemeConfig(themeConfig);
        roomRepository.save(room);
    }

    @Transactional
    public void updateDurations(UUID roomId, RoomDurationUpdateRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        if (request.getFocusDuration() != null) room.setFocusDuration(request.getFocusDuration());
        if (request.getBreakDuration() != null) room.setBreakDuration(request.getBreakDuration());
        roomRepository.save(room);
    }
    private Map<String, Object> buildTimerPayload(Room room) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomId", room.getId());
        payload.put("timerRunning", room.isTimerRunning());
        payload.put("timerStartedAt", room.getTimerStartedAt());
        payload.put("isOnBreak", room.isOnBreak());
        payload.put("focusDuration", room.getFocusDuration());
        payload.put("breakDuration", room.getBreakDuration());
        return payload;
    }
    @Transactional
    public void startPomodoroTimer(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setTimerRunning(true);
        room.setTimerStartedAt(Instant.now());
        room.setOnBreak(false);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/timer", buildTimerPayload(room));
    }

    @Transactional
    public void pausePomodoroTimer(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setTimerRunning(false);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/timer", buildTimerPayload(room));
    }

    @Transactional
    public void resumePomodoroTimer(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setTimerRunning(true);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/timer", buildTimerPayload(room));
    }

    @Transactional
    public void resetPomodoroTimer(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setTimerRunning(false);
        room.setTimerStartedAt(null);
        room.setOnBreak(false);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/timer", buildTimerPayload(room));
    }

}
