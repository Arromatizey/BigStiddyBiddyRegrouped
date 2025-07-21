package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.FriendshipId;
import com.esgi.studyBuddy.model.FriendshipStatus;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.FriendshipRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    private UUID fromId;
    private UUID toId;
    private User fromUser;
    private User toUser;

    @BeforeEach
    void setup() {
        fromId = UUID.randomUUID();
        toId = UUID.randomUUID();

        fromUser = new User();
        fromUser.setId(fromId);

        toUser = new User();
        toUser.setId(toId);
    }

    @Test
    void sendRequest_shouldSaveFriendship_whenNotExists() {
        FriendshipId id = new FriendshipId(fromId, toId);

        when(friendshipRepository.existsById(id)).thenReturn(false);
        when(userRepository.findById(fromId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toId)).thenReturn(Optional.of(toUser));

        friendshipService.sendRequest(fromId, toId);

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository).save(captor.capture());
        Friendship saved = captor.getValue();

        assertEquals(fromUser, saved.getRequester());
        assertEquals(toUser, saved.getTarget());
        assertEquals(FriendshipStatus.pending, saved.getStatus());
    }

    @Test
    void sendRequest_shouldNotSave_whenFriendshipExists() {
        FriendshipId id = new FriendshipId(fromId, toId);
        when(friendshipRepository.existsById(id)).thenReturn(true);

        friendshipService.sendRequest(fromId, toId);

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequest_shouldThrow_whenUserNotFound() {
        FriendshipId id = new FriendshipId(fromId, toId);
        when(friendshipRepository.existsById(id)).thenReturn(false);
        when(userRepository.findById(fromId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> friendshipService.sendRequest(fromId, toId));

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void acceptRequest_shouldUpdateStatusAndSave() {
        Friendship friendship = Friendship.builder()
                .requester(fromUser)
                .target(toUser)
                .status(FriendshipStatus.pending)
                .build();

        when(friendshipRepository.findByRequesterIdAndTargetId(fromId, toId)).thenReturn(Optional.of(friendship));

        friendshipService.acceptRequest(fromId, toId);

        assertEquals(FriendshipStatus.accepted, friendship.getStatus());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void acceptRequest_shouldThrow_whenFriendshipNotFound() {
        when(friendshipRepository.findByRequesterIdAndTargetId(fromId, toId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> friendshipService.acceptRequest(fromId, toId));

        verify(friendshipRepository, never()).save(any());
    }
}

