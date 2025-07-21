package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.DmMessage;
import com.esgi.studyBuddy.repository.DmMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DmMessageService {
    private final DmMessageRepository dmRepository;

    public List<DmMessage> getConversation(UUID userA, UUID userB) {
        return dmRepository.findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(userA, userB, userA, userB);
    }

    public void sendMessage(DmMessage message) {
        dmRepository.save(message);
    }
}
