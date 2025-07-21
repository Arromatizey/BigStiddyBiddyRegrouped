package com.esgi.studyBuddy.init;

import com.esgi.studyBuddy.DTO.AiMessageEvent;
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
        System.out.println("✅ Room created successfully.");

        // Post regular & AI-triggered messages
        roomMessageService.saveMessage(roomId, user.getId(), "This is a regular message.");
        System.out.println("💬 Regular message posted.");
        roomMessageService.saveMessageAndNotifyAI(roomId, user.getId(), "What is the quadratic formula?");
        System.out.println("🤖 AI-triggered message posted.");

        // Simulate timer flow
        roomService.startPomodoroTimer(roomId);
        System.out.println("⏱️ Timer started.");
        Thread.sleep(1000);
        roomService.pausePomodoroTimer(roomId);
        System.out.println("⏸️ Timer paused.");
        Thread.sleep(1000);
        roomService.resumePomodoroTimer(roomId);
        System.out.println("▶️ Timer resumed.");
        Thread.sleep(1000);
        roomService.resetPomodoroTimer(roomId);
        System.out.println("🔁 Timer reset.");

        // Simulate AI sending a response (to test Kafka listener)
        AiResponseEvent aiResponse = new AiResponseEvent(roomId, "The quadratic formula is x = (-b ± √(b²-4ac)) / 2a.");
        kafkaTemplate.send("ai-response-events", aiResponse);
        System.out.println("📤 Simulated AI response sent to Kafka.");


        Thread.sleep(4000);

        // 🧹 Cleanup
        roomMessageService.deleteMessagesByRoomId(roomId); // à implémenter si elle n'existe pas
        roomService.deleteRoomById(roomId);                // idem
        userRepository.deleteById(user.getId());
        userService.deleteUserByEmail("ai@studybuddy.com");
        System.out.println("🧽 Test data cleaned up.");
    }}
