package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.service.FriendshipService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(FriendshipController.class)
public class FriendshipControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendshipService friendshipService;

    @Test
    public void sendRequest_shouldReturnOk() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();

        Mockito.doNothing().when(friendshipService).sendRequest(from, to);

        mockMvc.perform(post("/api/friends/request")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(friendshipService).sendRequest(from, to);
    }

    @Test
    public void acceptRequest_shouldReturnOk() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();

        Mockito.doNothing().when(friendshipService).acceptRequest(from, to);

        mockMvc.perform(post("/api/friends/accept")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(friendshipService).acceptRequest(from, to);
    }
}

