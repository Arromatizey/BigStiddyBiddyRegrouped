package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.RoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface RoomMessageRepository extends JpaRepository<RoomMessage, UUID> {
    List<RoomMessage> findTop50ByRoom_IdOrderByCreatedAtDesc(UUID roomId);
}
