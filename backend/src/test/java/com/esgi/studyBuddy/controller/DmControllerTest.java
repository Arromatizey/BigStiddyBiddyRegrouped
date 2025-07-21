package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.model.DmMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.service.DmMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DmControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    @Mock
    private DmMessageService dmService;
    @InjectMocks
    private DmController dmController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dmController).build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getConversation_shouldReturnMessages() throws Exception {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        User sender = new User();
        sender.setId(userA);
        User receiver = new User();
        receiver.setId(userB);

        DmMessage message = DmMessage.builder()
                .id(UUID.randomUUID())
                .sender(sender)
                .receiver(receiver)
                .message("Hello")
                .createdAt(Instant.now())
                .build();

        List<DmMessage> messages = List.of(message);

        when(dmService.getConversation(userA, userB)).thenReturn(messages);

        mockMvc.perform(get("/api/dm")
                        .param("userA", userA.toString())
                        .param("userB", userB.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hello"))
                .andExpect(jsonPath("$[0].sender.id").value(userA.toString()))
                .andExpect(jsonPath("$[0].receiver.id").value(userB.toString()));

        verify(dmService).getConversation(userA, userB);
    }
}
