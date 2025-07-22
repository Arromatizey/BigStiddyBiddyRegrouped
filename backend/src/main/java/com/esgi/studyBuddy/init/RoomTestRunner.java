package com.esgi.studyBuddy.init;

import com.esgi.studyBuddy.DTO.AiResponseEvent;
import com.esgi.studyBuddy.model.Room;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.UserRepository;
import com.esgi.studyBuddy.service.RoomMessageService;
import com.esgi.studyBuddy.service.RoomService;
import com.esgi.studyBuddy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomTestRunner implements CommandLineRunner {

    private final RoomService roomService;
    private final UserRepository userRepository;
    private final RoomMessageService roomMessageService;
    private final KafkaTemplate<String, AiResponseEvent> kafkaTemplate;
    private final UserService userService;

    @Override
    public void run(String... args) throws InterruptedException {
        // 👤 Create a test user
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPassword("dummy");
        user.setDisplayName("Owner");
        user.setVerified(true);
        user = userRepository.save(user);

        System.out.println("\n✅✅✅ USER CREATED: Paste this User ID in your front:");
        System.out.println("➡️  User ID: " + user.getId() + "\n");

        // 🏠 Create a test room
        Room room = Room.builder()
                .owner(user)
                .subject("Pomodoro Test")
                .level("Any")
                .topic("Testing")
                .build();

        UUID roomId = roomService.createRoom(room);

        System.out.println("\n✅✅✅ ROOM CREATED: Paste this Room ID in your front:");
        System.out.println("➡️  Room ID: " + roomId + "\n");

        Thread.sleep(15000);

        // Send some test messages via RoomMessageService
        System.out.println("💬 Sending some test messages...");

        roomMessageService.saveMessage(roomId, user.getId(), "Hello everyone!");
        Thread.sleep(500);

        roomMessageService.saveMessage(roomId, user.getId(), "This is a normal message.");
        Thread.sleep(500);

        roomMessageService.saveMessageAndNotifyAI(roomId, user.getId(), "Hey AI, can you help me?");
        Thread.sleep(500);

        roomMessageService.saveMessage(roomId, user.getId(), "Let's test the chat functionality.");
        Thread.sleep(500);


        System.out.println("💬 Test messages sent!\n");

        // ⏱️ Trigger timer events that push WebSocket messages
        System.out.println("🟢 Starting Pomodoro Timer...");
        roomService.startPomodoroTimer(roomId);
        Thread.sleep(1000);

        System.out.println("⏸️ Pausing Pomodoro Timer...");
        roomService.pausePomodoroTimer(roomId);
        Thread.sleep(1000);

        System.out.println("▶️ Resuming Pomodoro Timer...");
        roomService.resumePomodoroTimer(roomId);
        Thread.sleep(1000);

        System.out.println("🔁 Resetting Pomodoro Timer...");
        roomService.resetPomodoroTimer(roomId);

        System.out.println("✅ Done! Open your test front and paste the Room ID and User ID to test chat and WebSocket events.\n");
    }
}
