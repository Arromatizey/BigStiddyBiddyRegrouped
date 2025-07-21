package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.RoomMessageRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomMessageServiceTest {

    @Mock
    private RoomMessageRepository messageRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate; // If you have a specific AiMessageEvent class, replace `Object`

    @InjectMocks
    private RoomMessageService roomMessageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLastMessages_shouldReturnTop50Messages() {
        UUID roomId = UUID.randomUUID();
        RoomMessage msg1 = new RoomMessage();
        RoomMessage msg2 = new RoomMessage();
        List<RoomMessage> messages = List.of(msg1, msg2);

        when(messageRepository.findTop50ByRoom_IdOrderByCreatedAtDesc(roomId)).thenReturn(messages);

        List<RoomMessage> result = roomMessageService.getLastMessages(roomId);

        assertEquals(messages, result);
        verify(messageRepository).findTop50ByRoom_IdOrderByCreatedAtDesc(roomId);
    }

    @Test
    void saveMessage_shouldSaveMessageToDatabase() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String content = "Hello world";

        Room room = new Room();
        room.setId(roomId);

        User user = new User();
        user.setId(userId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        roomMessageService.saveMessage(roomId, userId, content);

        ArgumentCaptor<RoomMessage> captor = ArgumentCaptor.forClass(RoomMessage.class);
        verify(messageRepository).save(captor.capture());

        RoomMessage saved = captor.getValue();
        assertEquals(content, saved.getMessage());
        assertEquals(room, saved.getRoom());
        assertEquals(user, saved.getUser());
    }

    @Test
    void saveMessage_shouldThrowIfRoomNotFound() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roomMessageService.saveMessage(roomId, userId, "test"));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void saveMessage_shouldThrowIfUserNotFound() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Room room = new Room();
        room.setId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roomMessageService.saveMessage(roomId, userId, "test"));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void saveMessageAndNotifyAI_shouldSaveAndSendKafkaEvent() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String content = "Hello AI";

        Room room = new Room();
        room.setId(roomId);
        room.setTopic("Biology");

        User user = new User();
        user.setId(userId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        roomMessageService.saveMessageAndNotifyAI(roomId, userId, content);

        verify(messageRepository).save(any());
        verify(kafkaTemplate).send(eq("ai-message-events"), any());
    }
}
