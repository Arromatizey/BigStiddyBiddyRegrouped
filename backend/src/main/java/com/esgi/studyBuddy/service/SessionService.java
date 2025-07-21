package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Session;
import com.esgi.studyBuddy.model.SessionState;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public UUID startSession(Session session) {
        session.setState(SessionState.Focus);
        session.setStartTime(Instant.now());
        return sessionRepository.save(session).getId();
    }

    @Transactional
    public void endSession(UUID id) {
        Session session = sessionRepository.findById(id).orElseThrow();
        session.setEndTime(Instant.now());
        session.setState(SessionState.Idle);
        sessionRepository.save(session);
    }
}
