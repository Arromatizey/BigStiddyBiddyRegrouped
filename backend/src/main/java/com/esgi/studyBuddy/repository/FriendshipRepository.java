package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    List<Friendship> findByRequester_IdOrTarget_Id(UUID requesterId, UUID targetId);
    Optional<Friendship> findByRequesterIdAndTargetId(UUID requesterId, UUID targetId);
}