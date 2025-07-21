package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.model.FocusStat;
import com.esgi.studyBuddy.service.FocusStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class FocusStatController {
    private final FocusStatService focusStatService;

    @GetMapping("/{userId}/week")
    public ResponseEntity<List<FocusStat>> getWeeklyStats(@PathVariable UUID userId) {
        return ResponseEntity.ok(focusStatService.getWeeklyStats(userId));
    }
}
