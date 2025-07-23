package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.DTO.DmMessageResponse;
import com.esgi.studyBuddy.DTO.DmMessageEvent;
import com.esgi.studyBuddy.model.DmMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.DmMessageRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DmMessageServiceTest {

    @Mock
    private DmMessageRepository dmRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private DmMessageService dmMessageService;

    @Test
    void getConversation_shouldReturnMessagesBetweenTwoUsers() {
        // Arrange
        UUID userAId = UUID.randomUUID();
        UUID userBId = UUID.randomUUID();

        User userA = new User();
        userA.setId(userAId);
        userA.setDisplayName("User A");

        User userB = new User();
        userB.setId(userBId);
        userB.setDisplayName("User B");

        DmMessage message1 = DmMessage.builder()
                .id(UUID.randomUUID())
                .sender(userA)
                .receiver(userB)
                .message("Salut !")
                .createdAt(Instant.now())
                .build();

        DmMessage message2 = DmMessage.builder()
                .id(UUID.randomUUID())
                .sender(userB)
                .receiver(userA)
                .message("Coucou !")
                .createdAt(Instant.now())
                .build();

        List<DmMessage> expectedMessages = List.of(message1, message2);

        when(dmRepository.findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(
                userAId, userBId, userAId, userBId))
                .thenReturn(expectedMessages);

        List<DmMessageResponse> result = dmMessageService.getConversation(userAId, userBId);

        assertEquals(2, result.size());
        assertEquals("Salut !", result.get(0).getMessage());
        assertEquals("Coucou !", result.get(1).getMessage());
        verify(dmRepository).findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(
                userAId, userBId, userAId, userBId);
    }

    @Test
    void sendMessage_shouldCallRepositorySave() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        User sender = new User();
        sender.setId(senderId);
        sender.setDisplayName("Sender");

        User receiver = new User();
        receiver.setId(receiverId);
        receiver.setDisplayName("Receiver");

        DmMessage savedMessage = DmMessage.builder()
                .id(UUID.randomUUID())
                .sender(sender)
                .receiver(receiver)
                .message("Hello!")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(dmRepository.save(org.mockito.ArgumentMatchers.any(DmMessage.class))).thenReturn(savedMessage);

        DmMessageResponse result = dmMessageService.sendMessage(senderId, receiverId, "Hello!");

        assertNotNull(result);
        assertEquals("Hello!", result.getMessage());
        assertEquals(senderId, result.getSenderId());
        assertEquals(receiverId, result.getReceiverId());
        verify(dmRepository).save(org.mockito.ArgumentMatchers.any(DmMessage.class));
        verify(messagingTemplate).convertAndSend("/user/" + receiverId + "/queue/dm", 
                org.mockito.ArgumentMatchers.any(DmMessageEvent.class));
    }
}