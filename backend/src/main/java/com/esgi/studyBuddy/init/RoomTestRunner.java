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

import java.util.List;
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

        List<String> conversation = List.of(
                "Hi, what are we studying today?",
                "Can someone explain what a variable is?",
                "I heard about quadratic equations, what are they?",
                "How do you solve them?",
                "What is the quadratic formula?"
        );

        for (int i = 0; i < conversation.size(); i++) {
            String msg = conversation.get(i);
            if (i == conversation.size() - 1) {
                // Last message triggers AI
                roomMessageService.saveMessageAndNotifyAI(roomId, user.getId(), msg);
                System.out.println("🤖 AI-triggered message posted: " + msg);
            } else {
                roomMessageService.saveMessage(roomId, user.getId(), msg);
                System.out.println("💬 Message posted: " + msg);
            }
            Thread.sleep(300); // simulate delay between messages
        }

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

        AiResponseEvent aiResponse = new AiResponseEvent(roomId, "The quadratic formula is x = (-b ± √(b²-4ac)) / 2a.");
        kafkaTemplate.send("ai-response-events", aiResponse);
        System.out.println("📤 Simulated AI response sent to Kafka.");

        Thread.sleep(4000);

        // 🧽 Cleanup test data
        roomMessageService.deleteMessagesByRoomId(roomId);
        roomService.deleteRoomById(roomId);
        userRepository.deleteById(user.getId());
        userService.deleteUserByEmail("ai@studybuddy.com");
        System.out.println("🧽 Test data cleaned up.");
    }
}
