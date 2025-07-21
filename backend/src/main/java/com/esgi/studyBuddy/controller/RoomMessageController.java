package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.DTO.PostRoomMessageRequest;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.service.RoomMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class RoomMessageController {

    private final RoomMessageService messageService;

    /**
     * GET /api/messages/room/{roomId}
     * Retrieve the last 50 messages in the specified room.
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<RoomMessage>> getMessages(@PathVariable UUID roomId) {
        return ResponseEntity.ok(messageService.getLastMessages(roomId));
    }

    /**
     * POST /api/messages/room/{roomId}
     * Regular message: store a user message without Kafka/AI trigger.
     */
    @PostMapping("/room/{roomId}")
    public ResponseEntity<Void> postRegularMessage(
            @PathVariable UUID roomId,
            @RequestBody PostRoomMessageRequest request) {
        messageService.saveMessage(roomId, request.userId(), request.message());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/messages/room/{roomId}/ai
     * Message to AI: store the message and publish an event to Kafka.
     */
    @PostMapping("/room/{roomId}/ai")
    public ResponseEntity<Void> postMessageToAI(
            @PathVariable UUID roomId,
            @RequestBody PostRoomMessageRequest request) {
        messageService.saveMessageAndNotifyAI(roomId, request.userId(), request.message());
        return ResponseEntity.ok().build();
    }
}
