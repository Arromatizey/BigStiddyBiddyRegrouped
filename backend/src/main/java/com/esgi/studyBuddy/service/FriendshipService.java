package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.controller.FriendshipNotificationController;
import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.FriendshipId;
import com.esgi.studyBuddy.model.FriendshipStatus;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.FriendshipRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipNotificationController notificationController;

    public List<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
    }

    @Transactional
    public void sendRequest(UUID from, UUID to) {
        FriendshipId id = new FriendshipId(from, to);
        if (!friendshipRepository.existsById(id)) {
            log.info("🤝 Creating new friend request from {} to {}", from, to);
            User requester = userRepository.findById(from).orElseThrow(() -> new IllegalArgumentException("Requester not found"));
            User target = userRepository.findById(to).orElseThrow(() -> new IllegalArgumentException("Target not found"));
            
            Friendship f = Friendship.builder()
                    .requesterId(from)
                    .targetId(to)
                    .requester(requester)
                    .target(target)
                    .status(FriendshipStatus.pending)
                    .build();
            friendshipRepository.save(f);
            log.info("✅ Friend request created successfully from {} to {}", requester.getEmail(), target.getEmail());
            
            // Send WebSocket notification
            notificationController.notifyFriendRequest(from, to, requester.getDisplayName() != null ? requester.getDisplayName() : requester.getEmail());
        } else {
            log.warn("⚠️ Friend request already exists from {} to {}", from, to);
        }
    }

    @Transactional
    public void acceptRequest(UUID from, UUID to) {
        log.info("✅ Accepting friend request from {} to {}", from, to);
        Friendship f = friendshipRepository.findByRequesterIdAndTargetId(from, to)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
        f.setStatus(FriendshipStatus.accepted);
        friendshipRepository.save(f);
        log.info("✅ Friend request accepted: {} and {} are now friends", f.getRequester().getEmail(), f.getTarget().getEmail());
        
        // Send WebSocket notification
        notificationController.notifyFriendAccepted(from, to, f.getTarget().getDisplayName() != null ? f.getTarget().getDisplayName() : f.getTarget().getEmail());
    }

    @Transactional
    public void rejectRequest(UUID from, UUID to) {
        log.info("❌ Rejecting friend request from {} to {}", from, to);
        friendshipRepository.deleteByRequesterIdAndTargetId(from, to);
        log.info("✅ Friend request deleted successfully");
    }

    public List<User> getAcceptedFriends(UUID userId) {
        log.debug("👥 Getting accepted friends for user: {}", userId);
        List<Friendship> friendships = friendshipRepository.findByStatusAndRequesterIdOrStatusAndTargetId(
                FriendshipStatus.accepted, userId,
                FriendshipStatus.accepted, userId
        );

        List<User> friends = friendships.stream()
                .map(f -> f.getRequester().getId().equals(userId) ? f.getTarget() : f.getRequester())
                .collect(Collectors.toList());
        
        log.debug("✅ Found {} accepted friends for user: {}", friends.size(), userId);
        return friends;
    }

    public List<Friendship> getPendingRequests(UUID userId) {
        log.debug("📬 Getting pending requests for user: {}", userId);
        List<Friendship> requests = friendshipRepository.findByTargetIdAndStatus(userId, FriendshipStatus.pending);
        log.debug("✅ Found {} pending requests for user: {}", requests.size(), userId);
        return requests;
    }

    public List<Friendship> getSentRequests(UUID userId) {
        log.debug("📤 Getting sent requests for user: {}", userId);
        List<Friendship> requests = friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.pending);
        log.debug("✅ Found {} sent requests for user: {}", requests.size(), userId);
        return requests;
    }

    public List<User> searchUsers(String query, UUID currentUserId) {
        log.debug("🔍 Searching users with query: '{}' excluding user: {}", query, currentUserId);
        List<User> users = userRepository.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
        log.debug("✅ Found {} users matching query: '{}'", users.size(), query);
        return users;
    }
}