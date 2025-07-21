package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.model.FocusStat;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.FocusStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FocusStatServiceTest {

    @Mock
    private FocusStatRepository statRepository;

    @InjectMocks
    private FocusStatService focusStatService;

    @Test
    void getWeeklyStats_shouldReturnStatsForUserOrderedByWeekDesc() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        FocusStat stat1 = new FocusStat();
        stat1.setUser(user);
        stat1.setWeekStart(LocalDate.now().minusWeeks(1));

        FocusStat stat2 = new FocusStat();
        stat2.setUser(user);
        stat2.setWeekStart(LocalDate.now().minusWeeks(2));

        List<FocusStat> expectedStats = List.of(stat1, stat2);

        when(statRepository.findByUser_IdOrderByWeekStartDesc(userId)).thenReturn(expectedStats);

        List<FocusStat> result = focusStatService.getWeeklyStats(userId);

        assertEquals(expectedStats, result);
        verify(statRepository).findByUser_IdOrderByWeekStartDesc(userId);
    }
}
