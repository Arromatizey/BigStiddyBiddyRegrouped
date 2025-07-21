package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.DmMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DmMessageRepository extends JpaRepository<DmMessage, UUID> {
    List<DmMessage> findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(
            UUID senderId, UUID receiverId, UUID receiverId2, UUID senderId2
    );
}
