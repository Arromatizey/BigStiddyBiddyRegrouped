package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.DTO.RoomDurationUpdateRequest;
import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRoom_shouldReturnRoomId() throws Exception {
        UUID fakeId = UUID.randomUUID();
        Room room = new Room();
        // Initialise room fields si besoin

        Mockito.when(roomService.createRoom(any(Room.class))).thenReturn(fakeId);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + fakeId + "\""));

    }

    @Test
    void updateTheme_shouldReturnOk() throws Exception {
        UUID roomId = UUID.randomUUID();
        String themeConfig = "\"dark-mode\"";

        Mockito.doNothing().when(roomService).updateTheme(eq(roomId), eq("dark-mode"));

        mockMvc.perform(patch("/api/rooms/" + roomId + "/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(themeConfig))
                .andExpect(status().isOk());
    }

    @Test
    void updateDurations_shouldReturnOk() throws Exception {
        UUID roomId = UUID.randomUUID();
        RoomDurationUpdateRequest request = new RoomDurationUpdateRequest();
        request.setFocusDuration(25);
        request.setBreakDuration(5);

        Mockito.doNothing().when(roomService).updateDurations(eq(roomId), any(RoomDurationUpdateRequest.class));

        mockMvc.perform(patch("/api/rooms/" + roomId + "/durations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void joinRoom_shouldReturnOk() throws Exception {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Mockito.doNothing().when(roomService).joinRoom(roomId, userId);

        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
    }
}

