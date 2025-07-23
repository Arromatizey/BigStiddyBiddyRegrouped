package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.DTO.ConversationSummary;
import com.esgi.studyBuddy.DTO.DmMessageRequest;
import com.esgi.studyBuddy.DTO.DmMessageResponse;
import com.esgi.studyBuddy.service.DmMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dm")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class DmController {
    private final DmMessageService dmService;

    @GetMapping("/users/{userId}/conversations")
    public ResponseEntity<List<ConversationSummary>> getUserConversations(@PathVariable UUID userId) {
        log.info("Getting conversations for user: {}", userId);
        return ResponseEntity.ok(dmService.getUserConversations(userId));
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<DmMessageResponse>> getConversation(
            @RequestParam UUID userA, 
            @RequestParam UUID userB) {
        log.info("Getting conversation between {} and {}", userA, userB);
        return ResponseEntity.ok(dmService.getConversation(userA, userB));
    }

    @PostMapping("/send")
    public ResponseEntity<DmMessageResponse> sendMessage(@RequestBody DmMessageRequest request) {
        log.info("Sending DM from {} to {}: {}", request.getSenderId(), request.getReceiverId(), request.getMessage());
        
        DmMessageResponse response = dmService.sendMessage(request.getSenderId(), request.getReceiverId(), request.getMessage());
        return ResponseEntity.ok(response);
    }
}
