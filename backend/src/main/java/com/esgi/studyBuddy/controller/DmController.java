package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.model.DmMessage;
import com.esgi.studyBuddy.service.DmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


import java.util.List;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmController {
    private final DmMessageService dmService;

    @GetMapping
    public ResponseEntity<List<DmMessage>> getConversation(@RequestParam UUID userA, @RequestParam UUID userB) {
        return ResponseEntity.ok(dmService.getConversation(userA, userB));
    }

    @PostMapping
    public ResponseEntity<Void> sendMessage(@RequestBody DmMessage message) {
        dmService.sendMessage(message);
        return ResponseEntity.ok().build();
    }
}
