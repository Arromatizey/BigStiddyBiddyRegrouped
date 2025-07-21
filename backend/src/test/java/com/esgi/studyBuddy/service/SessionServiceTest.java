package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Session;
import com.esgi.studyBuddy.model.SessionState;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startSession_shouldSetStateAndStartTimeAndReturnId() {
        Session session = new Session();
        UUID sessionId = UUID.randomUUID();
        session.setId(sessionId);

        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> {
            Session s = invocation.getArgument(0);
            s.setId(sessionId);
            return s;
        });

        UUID resultId = sessionService.startSession(session);

        assertEquals(sessionId, resultId);
        assertEquals(SessionState.Focus, session.getState());
        assertNotNull(session.getStartTime());
        verify(sessionRepository).save(session);
    }

    @Test
    void endSession_shouldSetEndTimeAndState() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setState(SessionState.Focus);
        session.setStartTime(Instant.now());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sessionService.endSession(sessionId);

        assertEquals(SessionState.Idle, session.getState());
        assertNotNull(session.getEndTime());
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository).save(session);
    }

    @Test
    void endSession_shouldThrowIfSessionNotFound() {
        UUID sessionId = UUID.randomUUID();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.endSession(sessionId));

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).save(any());
    }
}
