package com.esgi.studyBuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(FriendshipId.class)
public class Friendship {
    @Id
    @Column(name = "requester_id")
    private UUID requesterId;

    @Id
    @Column(name = "target_id")
    private UUID targetId;

    @ManyToOne
    @MapsId("requesterId")
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @MapsId("targetId")
    @JoinColumn(name = "target_id")
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.pending;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
