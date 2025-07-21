package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.model.Prompt;
import com.esgi.studyBuddy.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {
    private final PromptService promptService;

    @PostMapping("/room/{roomId}")
    public ResponseEntity<Prompt> askPrompt(@PathVariable UUID roomId, @RequestBody Prompt prompt) {
        return ResponseEntity.ok(promptService.handlePrompt(roomId, prompt));
    }
}
