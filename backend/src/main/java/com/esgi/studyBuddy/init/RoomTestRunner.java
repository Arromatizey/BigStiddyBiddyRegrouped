package com.esgi.studyBuddy.init;

import com.esgi.studyBuddy.model.*;
import com.esgi.studyBuddy.repository.UserRepository;
import com.esgi.studyBuddy.service.RoomMessageService;
import com.esgi.studyBuddy.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomTestRunner implements CommandLineRunner {

    private final RoomService roomService;
    private final UserRepository userRepository;
    private final RoomMessageService roomMessageService;

    @Override
    public void run(String... args) throws InterruptedException {
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPassword("dummy");
        user.setDisplayName("Owner");
        user.setVerified(true);
        user = userRepository.save(user);

        Room room = Room.builder()
                .owner(user)
                .subject("Math")
                .level("Beginner")
                .topic("Algebra")
                .build();

        UUID roomId = roomService.createRoom(room);
        System.out.println("Room created successfully.");

        // Post messages
        roomMessageService.saveMessage(roomId, user.getId(), "This is a regular message.");
        System.out.println("Regular message posted.");
        roomMessageService.saveMessageAndNotifyAI(roomId, user.getId(), "What is the quadratic formula?");
        System.out.println("AI-triggered message posted.");

        // Timer logic
        roomService.startPomodoroTimer(roomId);
        System.out.println("Timer started.");

        Thread.sleep(2000); // simulate wait time

        roomService.pausePomodoroTimer(roomId);
        System.out.println("Timer paused.");

        roomService.resumePomodoroTimer(roomId);
        System.out.println("Timer resumed.");

        roomService.resetPomodoroTimer(roomId);
        System.out.println("Timer reset.");

    }

}

