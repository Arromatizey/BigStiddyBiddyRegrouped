package com.esgi.studyBuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "focus_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(FocusStatId.class)
public class FocusStat {
    @Id
    private UUID userId;

    @Id
    private LocalDate weekStart;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer focusMinutes = 0;
}
