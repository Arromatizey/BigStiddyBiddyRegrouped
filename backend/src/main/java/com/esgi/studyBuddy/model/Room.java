package com.esgi.studyBuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String level;

    private String topic;
    private String institution;

    @Column(nullable = false)
    @Builder.Default
    private Integer focusDuration = 25;

    @Column(nullable = false)
    @Builder.Default
    private Integer breakDuration = 5;

    @Column(columnDefinition = "text")
    private String themeConfig;

    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "timer_running")
    private boolean timerRunning = false;

    @Column(name = "timer_started_at")
    private Instant timerStartedAt;

    @Column(name = "is_on_break")
    private boolean isOnBreak = false;

}
