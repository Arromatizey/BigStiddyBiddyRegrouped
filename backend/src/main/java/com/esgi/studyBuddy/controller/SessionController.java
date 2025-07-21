package com.esgi.studyBuddy.controller;
import com.esgi.studyBuddy.model.Session;

import com.esgi.studyBuddy.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<UUID> startSession(@RequestBody Session session) {
        return ResponseEntity.ok(sessionService.startSession(session));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<Void> endSession(@PathVariable UUID id) {
        sessionService.endSession(id);
        return ResponseEntity.ok().build();
    }
}
