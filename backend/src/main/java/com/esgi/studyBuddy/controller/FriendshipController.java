package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {
    private final FriendshipService friendshipService;

    @GetMapping("/friendships")
    public ResponseEntity<List<Friendship>> getAllFriendships() {
        log.info("🤝 Getting all friendships");
        return ResponseEntity.ok(friendshipService.getAllFriendships());
    }

    @GetMapping("/users/{userId}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable UUID userId) {
        log.info("👥 Getting friends for user: {}", userId);
        List<User> friends = friendshipService.getAcceptedFriends(userId);
        log.info("✅ Found {} friends for user: {}", friends.size(), userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/users/{userId}/pending-requests")
    public ResponseEntity<List<Friendship>> getPendingRequests(@PathVariable UUID userId) {
        log.info("📬 Getting pending requests for user: {}", userId);
        List<Friendship> requests = friendshipService.getPendingRequests(userId);
        log.info("✅ Found {} pending requests for user: {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/users/{userId}/sent-requests")
    public ResponseEntity<List<Friendship>> getSentRequests(@PathVariable UUID userId) {
        log.info("📤 Getting sent requests for user: {}", userId);
        List<Friendship> requests = friendshipService.getSentRequests(userId);
        log.info("✅ Found {} sent requests for user: {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query, @RequestParam UUID currentUserId) {
        log.info("🔍 Searching users with query: '{}' for user: {}", query, currentUserId);
        List<User> users = friendshipService.searchUsers(query, currentUserId);
        log.info("✅ Found {} users matching query: '{}'", users.size(), query);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(@RequestParam UUID from, @RequestParam UUID to) {
        log.info("🤝 Sending friend request from: {} to: {}", from, to);
        friendshipService.sendRequest(from, to);
        log.info("✅ Friend request sent successfully from: {} to: {}", from, to);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRequest(@RequestParam UUID from, @RequestParam UUID to) {
        log.info("✅ Accepting friend request from: {} to: {}", from, to);
        friendshipService.acceptRequest(from, to);
        log.info("✅ Friend request accepted successfully from: {} to: {}", from, to);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejectRequest(@RequestParam UUID from, @RequestParam UUID to) {
        log.info("❌ Rejecting friend request from: {} to: {}", from, to);
        friendshipService.rejectRequest(from, to);
        log.info("✅ Friend request rejected successfully from: {} to: {}", from, to);
        return ResponseEntity.ok().build();
    }
}
