package com.esgi.studyBuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FriendshipNotificationController {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyFriendRequest(UUID fromUserId, UUID toUserId, String fromUserName) {
        log.info("📨 Sending friend request notification from {} to {}", fromUserId, toUserId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FRIEND_REQUEST");
        notification.put("userId", fromUserId.toString());
        notification.put("data", Map.of(
            "fromUserId", fromUserId.toString(),
            "fromUserName", fromUserName,
            "message", fromUserName + " vous a envoyé une demande d'ami"
        ));
        
        messagingTemplate.convertAndSend("/topic/users/" + toUserId + "/friends", notification);
        log.info("✅ Friend request notification sent to user: {}", toUserId);
    }

    public void notifyFriendAccepted(UUID fromUserId, UUID toUserId, String acceptedUserName) {
        log.info("✅ Sending friend accepted notification from {} to {}", toUserId, fromUserId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FRIEND_ACCEPTED");
        notification.put("userId", toUserId.toString());
        notification.put("data", Map.of(
            "acceptedUserId", toUserId.toString(),
            "acceptedUserName", acceptedUserName,
            "message", acceptedUserName + " a accepté votre demande d'ami"
        ));
        
        messagingTemplate.convertAndSend("/topic/users/" + fromUserId + "/friends", notification);
        log.info("✅ Friend accepted notification sent to user: {}", fromUserId);
    }

    public void notifyOnlineStatusChange(UUID userId, boolean isOnline) {
        log.info("📡 Broadcasting online status change for user: {} - Online: {}", userId, isOnline);
        
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("userId", userId.toString());
        statusUpdate.put("isOnline", isOnline);
        statusUpdate.put("lastSeenAt", System.currentTimeMillis());
        
        // Broadcast to all friends status topic
        messagingTemplate.convertAndSend("/topic/friends/status", statusUpdate);
        log.info("✅ Online status update broadcast for user: {}", userId);
    }
} 