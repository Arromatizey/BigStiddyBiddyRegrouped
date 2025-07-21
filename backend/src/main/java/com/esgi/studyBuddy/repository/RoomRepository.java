package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByOwner_Id(UUID ownerId);
}
