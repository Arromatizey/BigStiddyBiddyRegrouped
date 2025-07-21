package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.FriendshipId;
import com.esgi.studyBuddy.model.FriendshipStatus;
import com.esgi.studyBuddy.repository.FriendshipRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public List<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
    }

    @Transactional
    public void sendRequest(UUID from, UUID to) {
        FriendshipId id = new FriendshipId(from, to);
        if (!friendshipRepository.existsById(id)) {
            Friendship f = Friendship.builder()
                    .requester(userRepository.findById(from).orElseThrow())
                    .target(userRepository.findById(to).orElseThrow())
                    .status(FriendshipStatus.pending)
                    .build();
            friendshipRepository.save(f);
        }
    }

    @Transactional
    public void acceptRequest(UUID from, UUID to) {
        Friendship f = friendshipRepository.findByRequesterIdAndTargetId(from, to).orElseThrow();
        f.setStatus(FriendshipStatus.accepted);
        friendshipRepository.save(f);
    }
}