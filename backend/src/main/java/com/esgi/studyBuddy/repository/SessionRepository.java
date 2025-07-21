package com.esgi.studyBuddy.repository;
import com.esgi.studyBuddy.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByRoom_Id(UUID roomId);
}
