package com.esgi.studyBuddy.controller;


import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.service.RoomMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomMessageService messageService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMessages_shouldReturnListOfMessages() throws Exception {
        UUID roomId = UUID.randomUUID();

        // Création des objets sans builder
        Room room = new Room();
        room.setId(roomId);

        User user = new User();
        user.setId(UUID.randomUUID());

        RoomMessage message = new RoomMessage();
        message.setId(UUID.randomUUID());
        message.setRoom(room);
        message.setUser(user);
        message.setMessage("Hello world!");
        message.setCreatedAt(Instant.now());

        when(messageService.getLastMessages(roomId)).thenReturn(List.of(message));

        mockMvc.perform(get("/api/messages/room/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hello world!"))
                .andExpect(jsonPath("$[0].room.id").value(roomId.toString()))
                .andExpect(jsonPath("$[0].user.id").value(user.getId().toString()));
    }
}

