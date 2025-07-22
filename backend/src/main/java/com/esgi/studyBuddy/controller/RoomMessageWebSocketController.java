package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.DTO.PostRoomMessageRequest;
import com.esgi.studyBuddy.model.RoomMessage;
import com.esgi.studyBuddy.service.RoomMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RoomMessageWebSocketController {

    private final RoomMessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roomId}/send")
    public void handleRoomMessage(@DestinationVariable UUID roomId, PostRoomMessageRequest request) {
        // Save message to DB
        messageService.saveMessage(roomId, request.userId(), request.message());

        // Retrieve last message (or build message object to broadcast)
        RoomMessage message = messageService.getLastMessages(roomId)
                .stream()
                .findFirst()
                .orElse(null);

        // Broadcast to all subscribers of the room topic
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
    }
}
