package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.model.Session;
import com.esgi.studyBuddy.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testStartSession() throws Exception {
        UUID fakeId = UUID.randomUUID();

        Session session = new Session();
        session.setStartTime(Instant.now());
        session.setRoom(null); // ou simule un Room si nécessaire

        when(sessionService.startSession(any(Session.class))).thenReturn(fakeId);

        mockMvc.perform(post("/api/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(session)))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + fakeId.toString() + "\""));

        verify(sessionService, times(1)).startSession(any(Session.class));
    }

    @Test
    void testEndSession() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(patch("/api/sessions/{id}/end", sessionId))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).endSession(sessionId);
    }
}
