package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.RoomMember;
import com.esgi.studyBuddy.model.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {
    List<RoomMember> findByRoom_Id(UUID roomId);
    List<RoomMember> findByUser_Id(UUID userId);
    Optional<RoomMember> findByUserIdAndRoomId(UUID userId, UUID roomId);
}
