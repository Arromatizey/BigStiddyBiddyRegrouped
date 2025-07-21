package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.model.Prompt;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.service.PromptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PromptController.class)
public class PromptControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromptService promptService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void askPrompt_shouldReturnPrompt() throws Exception {
        UUID roomId = UUID.randomUUID();

        Prompt inputPrompt = Prompt.builder()
                .promptText("Quelle est la capitale de la France ?")
                .build();

        Room room = new Room();
        room.setId(roomId);

        Prompt returnedPrompt = Prompt.builder()
                .id(UUID.randomUUID())
                .room(room)
                .promptText("Quelle est la capitale de la France ?")
                .aiResponse("Paris")
                .createdAt(Instant.now())
                .build();

        Mockito.when(promptService.handlePrompt(eq(roomId), any(Prompt.class))).thenReturn(returnedPrompt);

        mockMvc.perform(post("/api/prompts/room/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputPrompt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedPrompt.getId().toString()))
                .andExpect(jsonPath("$.promptText").value("Quelle est la capitale de la France ?"))
                .andExpect(jsonPath("$.aiResponse").value("Paris"))
                .andExpect(jsonPath("$.room.id").value(roomId.toString()))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}

