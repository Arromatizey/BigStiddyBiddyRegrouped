package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.FocusStat;
import com.esgi.studyBuddy.model.FocusStatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface FocusStatRepository extends JpaRepository<FocusStat, FocusStatId> {
    List<FocusStat> findByUser_IdOrderByWeekStartDesc(UUID userId);
    Optional<FocusStat> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}
