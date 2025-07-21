package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.DTO.RoomDurationUpdateRequest;
import com.esgi.studyBuddy.DTO.AiRoomJoinEvent;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.RoomMember;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.RoomMemberRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomMemberRepository roomMemberRepository;
    @Mock
    private KafkaTemplate<String, AiRoomJoinEvent> kafkaTemplate;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testCreateRoom() {
        // Create and mock owner user
        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@example.com");
        owner.setPassword("dummy");
        owner.setDisplayName("Owner");
        owner.setVerified(true);

        // Build the room with the owner
        Room room = Room.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .subject("Math")
                .level("Beginner")
                .topic("Algebra")
                .build();

        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // Mock saving room members
        when(roomMemberRepository.save(any(RoomMember.class))).thenReturn(null);

        // Mock the AI user
        User aiUser = new User();
        aiUser.setId(UUID.randomUUID());
        aiUser.setEmail("ai@studybuddy.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(aiUser));

        // Mock Kafka send to return a completed CompletableFuture with correct generic type
        RecordMetadata metadata = new RecordMetadata(null, 0L, 0L, 0L, 0L, Math.toIntExact(0L), 0);

        SendResult<String, AiRoomJoinEvent> sendResult = new SendResult<>(null, metadata);
        CompletableFuture<SendResult<String, AiRoomJoinEvent>> future = new CompletableFuture<>();
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), any(AiRoomJoinEvent.class))).thenReturn(future);

        // Act
        UUID roomId = roomService.createRoom(room);

        // Assert
        assertEquals(room.getId(), roomId);
        verify(roomRepository).save(room);
        verify(roomMemberRepository, atLeastOnce()).save(any());
    }



    @Test
    void testJoinRoom() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Room room = new Room();
        room.setId(roomId);

        User user = new User();
        user.setId(userId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomMemberRepository.save(any(RoomMember.class))).thenReturn(new RoomMember());

        assertDoesNotThrow(() -> roomService.joinRoom(roomId, userId));
        verify(roomMemberRepository).save(any(RoomMember.class));
        // Additional verifications for Kafka/SimpMessagingTemplate as needed
    }

    @Test
    void testUpdateTheme() {
        UUID roomId = UUID.randomUUID();
        String newTheme = "Dark";
        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        assertDoesNotThrow(() -> roomService.updateTheme(roomId, newTheme));
        verify(roomRepository).save(room);
        assertEquals(newTheme, room.getThemeConfig());
    }

    @Test
    void testUpdateDurations() {
        UUID roomId = UUID.randomUUID();
        RoomDurationUpdateRequest req = new RoomDurationUpdateRequest();
        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        assertDoesNotThrow(() -> roomService.updateDurations(roomId, req));
        verify(roomRepository).save(room);
    }

    @Test
    void testStartPomodoroTimer() {
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        assertDoesNotThrow(() -> roomService.startPomodoroTimer(roomId));
        verify(roomRepository).save(room);
    }

    @Test
    void testPausePomodoroTimer() {
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        assertDoesNotThrow(() -> roomService.pausePomodoroTimer(roomId));
        verify(roomRepository).save(room);
    }

    @Test
    void testResumePomodoroTimer() {
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        assertDoesNotThrow(() -> roomService.resumePomodoroTimer(roomId));
        verify(roomRepository).save(room);
    }

    @Test
    void testResetPomodoroTimer() {
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        assertDoesNotThrow(() -> roomService.resetPomodoroTimer(roomId));
        verify(roomRepository).save(room);
    }
}
