package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;

    @GetMapping("/friendships")
    public ResponseEntity<List<Friendship>> getAllFriendships() {
        return ResponseEntity.ok(friendshipService.getAllFriendships());
    }

    @GetMapping("/users/{userId}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable UUID userId) {
        return ResponseEntity.ok(friendshipService.getAcceptedFriends(userId));
    }



    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(@RequestParam UUID from, @RequestParam UUID to) {
        friendshipService.sendRequest(from, to);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRequest(@RequestParam UUID from, @RequestParam UUID to) {
        friendshipService.acceptRequest(from, to);
        return ResponseEntity.ok().build();
    }
}
