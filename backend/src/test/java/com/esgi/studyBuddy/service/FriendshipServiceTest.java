package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.controller.FriendshipNotificationController;
import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.FriendshipStatus;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.FriendshipRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private FriendshipNotificationController notificationController;

    @InjectMocks
    private FriendshipService friendshipService;

    private User user1;
    private User user2;
    private UUID userId1;
    private UUID userId2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        user1 = new User();
        user1.setId(userId1);
        user1.setEmail("user1@example.com");
        user1.setDisplayName("User One");

        user2 = new User();
        user2.setId(userId2);
        user2.setEmail("user2@example.com");
        user2.setDisplayName("User Two");
    }

    @Test
    void testSendRequest_Success() {
        // Given
        when(friendshipRepository.existsById(any())).thenReturn(false);
        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        // When
        friendshipService.sendRequest(userId1, userId2);

        // Then
        verify(friendshipRepository).save(any(Friendship.class));
        verify(notificationController).notifyFriendRequest(userId1, userId2, "User One");
    }

    @Test
    void testSendRequest_AlreadyExists() {
        // Given
        when(friendshipRepository.existsById(any())).thenReturn(true);

        // When
        friendshipService.sendRequest(userId1, userId2);

        // Then
        verify(friendshipRepository, never()).save(any());
        verify(notificationController, never()).notifyFriendRequest(any(), any(), any());
    }

    @Test
    void testAcceptRequest_Success() {
        // Given
        Friendship friendship = Friendship.builder()
                .requesterId(userId1)
                .targetId(userId2)
                .requester(user1)
                .target(user2)
                .status(FriendshipStatus.pending)
                .build();

        when(friendshipRepository.findByRequesterIdAndTargetId(userId1, userId2))
                .thenReturn(Optional.of(friendship));

        // When
        friendshipService.acceptRequest(userId1, userId2);

        // Then
        assertEquals(FriendshipStatus.accepted, friendship.getStatus());
        verify(friendshipRepository).save(friendship);
        verify(notificationController).notifyFriendAccepted(userId1, userId2, "User Two");
    }

    @Test
    void testRejectRequest() {
        // When
        friendshipService.rejectRequest(userId1, userId2);

        // Then
        verify(friendshipRepository).deleteByRequesterIdAndTargetId(userId1, userId2);
    }

    @Test
    void testGetAcceptedFriends() {
        // Given
        Friendship friendship = Friendship.builder()
                .requesterId(userId1)
                .targetId(userId2)
                .requester(user1)
                .target(user2)
                .status(FriendshipStatus.accepted)
                .build();

        when(friendshipRepository.findByStatusAndRequesterIdOrStatusAndTargetId(
                FriendshipStatus.accepted, userId1,
                FriendshipStatus.accepted, userId1))
                .thenReturn(Arrays.asList(friendship));

        // When
        List<User> friends = friendshipService.getAcceptedFriends(userId1);

        // Then
        assertEquals(1, friends.size());
        assertEquals(user2, friends.get(0));
    }

    @Test
    void testSearchUsers() {
        // Given
        String query = "user";
        when(userRepository.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query))
                .thenReturn(Arrays.asList(user1, user2));

        // When
        List<User> results = friendshipService.searchUsers(query, userId1);

        // Then
        assertEquals(1, results.size());
        assertEquals(user2, results.get(0));
    }
}

