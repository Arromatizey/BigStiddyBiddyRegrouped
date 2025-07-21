package com.esgi.studyBuddy.DTO;

import java.time.Instant;

public record PomodoroTimerStatus(boolean running, boolean isOnBreak, Instant startedAt, int focusDuration, int breakDuration) {}
