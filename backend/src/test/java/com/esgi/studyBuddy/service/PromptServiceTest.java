package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Prompt;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.repository.PromptRepository;
import com.esgi.studyBuddy.repository.RoomRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromptServiceTest {

    @Mock
    private PromptRepository promptRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PromptService promptService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handlePrompt_shouldSetRoomAndAiResponseIfNull_thenSave() {
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);

        Prompt prompt = new Prompt();
        prompt.setAiResponse(null);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(promptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Prompt result = promptService.handlePrompt(roomId, prompt);

        assertEquals(room, result.getRoom());
        assertNotNull(result.getAiResponse());
        assertEquals("Réponse automatique IA", result.getAiResponse());

        verify(roomRepository).findById(roomId);
        verify(promptRepository).save(prompt);
    }

    @Test
    void handlePrompt_shouldUseExistingAiResponseIfNotNull() {
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);

        Prompt prompt = new Prompt();
        prompt.setAiResponse("Réponse personnalisée");

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(promptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Prompt result = promptService.handlePrompt(roomId, prompt);

        assertEquals(room, result.getRoom());
        assertEquals("Réponse personnalisée", result.getAiResponse());

        verify(roomRepository).findById(roomId);
        verify(promptRepository).save(prompt);
    }

    @Test
    void handlePrompt_shouldThrowIfRoomNotFound() {
        UUID roomId = UUID.randomUUID();
        Prompt prompt = new Prompt();

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> promptService.handlePrompt(roomId, prompt));

        verify(roomRepository).findById(roomId);
        verifyNoInteractions(promptRepository);
    }
}
