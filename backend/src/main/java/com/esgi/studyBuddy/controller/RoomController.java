package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.DTO.RoomDurationUpdateRequest;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<UUID> createRoom(@RequestBody Room room) {
        UUID id = roomService.createRoom(room);
        return ResponseEntity.ok(id);
    }

    @PatchMapping("/{id}/theme")
    public ResponseEntity<Void> updateTheme(@PathVariable UUID id, @RequestBody String themeConfig) {
        roomService.updateTheme(id, themeConfig);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/durations")
    public ResponseEntity<Void> updateDurations(@PathVariable UUID id, @RequestBody RoomDurationUpdateRequest request) {
        roomService.updateDurations(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinRoom(@PathVariable UUID roomId, @RequestParam UUID userId) {
        roomService.joinRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/room/{roomId}/start-timer")
    public ResponseEntity<Void> startTimer(@PathVariable UUID roomId) {
        roomService.startPomodoroTimer(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/{roomId}/stop-timer")
    public ResponseEntity<Void> stopTimer(@PathVariable UUID roomId) {
        roomService.resetPomodoroTimer(roomId);
        return ResponseEntity.ok().build();
    }
}
