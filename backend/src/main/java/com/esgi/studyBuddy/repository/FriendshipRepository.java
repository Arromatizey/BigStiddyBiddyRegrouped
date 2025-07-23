package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.Friendship;
import com.esgi.studyBuddy.model.FriendshipId;
import com.esgi.studyBuddy.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    List<Friendship> findByRequester_IdOrTarget_Id(UUID requesterId, UUID targetId);
    Optional<Friendship> findByRequesterIdAndTargetId(UUID requesterId, UUID targetId);
    List<Friendship> findByStatusAndRequesterIdOrStatusAndTargetId(
            FriendshipStatus status1, UUID requesterId,
            FriendshipStatus status2, UUID targetId
    );
    List<Friendship> findByTargetIdAndStatus(UUID targetId, FriendshipStatus status);
    List<Friendship> findByRequesterIdAndStatus(UUID requesterId, FriendshipStatus status);
    
    @Modifying
    @Transactional
    void deleteByRequesterIdAndTargetId(UUID requesterId, UUID targetId);

}