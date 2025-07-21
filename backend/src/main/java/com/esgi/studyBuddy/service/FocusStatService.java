package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.FocusStat;
import com.esgi.studyBuddy.repository.FocusStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FocusStatService {
    private final FocusStatRepository statRepository;

    public List<FocusStat> getWeeklyStats(UUID userId) {
        return statRepository.findByUser_IdOrderByWeekStartDesc(userId);
    }
}
