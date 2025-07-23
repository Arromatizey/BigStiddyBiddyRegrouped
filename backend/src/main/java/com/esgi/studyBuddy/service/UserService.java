package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.DTO.UserUpdateRequest;
import com.esgi.studyBuddy.controller.FriendshipNotificationController;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipNotificationController notificationController;

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UUID createUser(User user) {
        return userRepository.save(user).getId();
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        userRepository.deleteUserByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public void updateOwnInfo(User user, UserUpdateRequest updateRequest) {
        if (updateRequest.getDisplayName() != null) {
            user.setDisplayName(updateRequest.getDisplayName());
        }
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateLastSeen(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            boolean wasOffline = !user.isOnline();
            user.setLastSeenAt(Instant.now());
            userRepository.save(user);
            
            // Notify online status change if user came back online
            if (wasOffline) {
                log.info("📡 User {} is now online", userId);
                notificationController.notifyOnlineStatusChange(userId, true);
            }
        });
    }

}
