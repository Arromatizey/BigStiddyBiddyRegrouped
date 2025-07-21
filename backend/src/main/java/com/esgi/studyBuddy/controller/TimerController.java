package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/timer")
@RequiredArgsConstructor
public class TimerController {
    private final RoomService roomService;

    @PostMapping("/{roomId}/start")
    public ResponseEntity<Void> start(@PathVariable UUID roomId) {
        roomService.startPomodoroTimer(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/pause")
    public ResponseEntity<Void> pause(@PathVariable UUID roomId) {
        roomService.pausePomodoroTimer(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/resume")
    public ResponseEntity<Void> resume(@PathVariable UUID roomId) {
        roomService.resumePomodoroTimer(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/reset")
    public ResponseEntity<Void> reset(@PathVariable UUID roomId) {
        roomService.resetPomodoroTimer(roomId);
        return ResponseEntity.ok().build();
    }
}
