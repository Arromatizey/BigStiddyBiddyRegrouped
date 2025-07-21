package com.esgi.studyBuddy.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class TimerMessage {
    private UUID roomId;
    private String action; // start, pause, resume, reset
}
