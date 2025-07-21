package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.DmMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.DmMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DmMessageServiceTest {

    @Mock
    private DmMessageRepository dmRepository;

    @InjectMocks
    private DmMessageService dmMessageService;

    @Test
    void getConversation_shouldReturnMessagesBetweenTwoUsers() {
        // Arrange
        UUID userAId = UUID.randomUUID();
        UUID userBId = UUID.randomUUID();

        User userA = new User();
        userA.setId(userAId);

        User userB = new User();
        userB.setId(userBId);

        DmMessage message1 = new DmMessage();
        message1.setSender(userA);
        message1.setReceiver(userB);
        message1.setMessage("Salut !");
        message1.setCreatedAt(Instant.now());

        DmMessage message2 = new DmMessage();
        message2.setSender(userB);
        message2.setReceiver(userA);
        message2.setMessage("Coucou !");
        message2.setCreatedAt(Instant.now());

        List<DmMessage> expectedMessages = List.of(message1, message2);

        when(dmRepository.findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(
                userAId, userBId, userAId, userBId))
                .thenReturn(expectedMessages);

        List<DmMessage> result = dmMessageService.getConversation(userAId, userBId);

        assertEquals(expectedMessages, result);
        verify(dmRepository).findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(
                userAId, userBId, userAId, userBId);
    }

    @Test
    void sendMessage_shouldCallRepositorySave() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        User sender = new User();
        sender.setId(senderId);

        User receiver = new User();
        receiver.setId(receiverId);

        DmMessage message = new DmMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMessage("Hello!");
        message.setCreatedAt(Instant.now());

        dmMessageService.sendMessage(message);

        verify(dmRepository).save(message);
    }
}