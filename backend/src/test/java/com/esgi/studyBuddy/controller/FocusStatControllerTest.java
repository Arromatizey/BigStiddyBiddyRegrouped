package com.esgi.studyBuddy.controller;

import com.esgi.studyBuddy.controller.FocusStatController;
import com.esgi.studyBuddy.jwt.JwtUtil;
import com.esgi.studyBuddy.model.FocusStat;
import com.esgi.studyBuddy.service.FocusStatService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(FocusStatController.class)
public class FocusStatControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FocusStatService focusStatService;

    @Test
    void getWeeklyStats_shouldReturnFocusStats() throws Exception {
        UUID userId = UUID.randomUUID();

        FocusStat stat1 = new FocusStat();
        stat1.setWeekStart(LocalDate.of(2023, 5, 1));
        FocusStat stat2 = new FocusStat();
        stat2.setWeekStart(LocalDate.of(2023, 5, 8));

        List<FocusStat> stats = List.of(stat1, stat2);

        Mockito.when(focusStatService.getWeeklyStats(userId)).thenReturn(stats);

        mockMvc.perform(get("/api/stats/{userId}/week", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(stats.size()))
                .andExpect(jsonPath("$[0].weekStart").value("2023-05-01"))
                .andExpect(jsonPath("$[1].weekStart").value("2023-05-08"));
    }
}
