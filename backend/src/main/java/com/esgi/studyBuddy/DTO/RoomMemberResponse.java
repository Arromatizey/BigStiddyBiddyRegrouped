package com.esgi.studyBuddy.DTO;

import com.esgi.studyBuddy.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RoomMemberResponse {
    private UUID userId;
    private String displayName;
    private String email;
    private UserRole role;
}
