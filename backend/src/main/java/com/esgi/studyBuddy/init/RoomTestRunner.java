package com.esgi.studyBuddy.init;

import com.esgi.studyBuddy.DTO.AiResponseEvent;
import com.esgi.studyBuddy.DTO.RoomMemberResponse;
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

// @Component - Désactivé pour éviter les conflits de données de test
@RequiredArgsConstructor
public class RoomTestRunner implements CommandLineRunner {

    private final RoomService roomService;
    private final UserRepository userRepository;
    private final RoomMessageService roomMessageService;
    private final KafkaTemplate<String, AiResponseEvent> kafkaTemplate;
    private final UserService userService;

    @Override
    public void run(String... args) throws InterruptedException {
        // 👤 Créer ou récupérer un utilisateur
        User user = userRepository.findByEmail("owner@example.com")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail("owner@example.com");
                    newUser.setPassword("dummy");
                    newUser.setDisplayName("Owner");
                    newUser.setVerified(true);
                    return userRepository.save(newUser);
                });

        // 🏠 Créer une room
        Room room = Room.builder()
                .owner(user)
                .subject("Math")
                .level("Beginner")
                .topic("Algebra")
                .build();

        UUID roomId = roomService.createRoom(room);
        System.out.println("✅ Room created successfully with ID: " + roomId);

        // 💬 Simulation d'une conversation
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
                roomMessageService.saveMessageAndNotifyAI(roomId, user.getId(), msg);
                System.out.println("🤖 AI-triggered message posted: " + msg);
            } else {
                roomMessageService.saveMessage(roomId, user.getId(), msg);
                System.out.println("💬 Message posted: " + msg);
            }
            Thread.sleep(300);
        }

        // ⏱️ Tester le timer Pomodoro
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

        // 🧠 Simulation d'une réponse IA
        AiResponseEvent aiResponse = new AiResponseEvent(roomId, "The quadratic formula is x = (-b ± √(b²-4ac)) / 2a.");
        kafkaTemplate.send("ai-response-events", aiResponse);
        System.out.println("📤 Simulated AI response sent to Kafka.");

        // 🔍 TEST getRoomById
        Room fetchedRoom = roomService.getRoomById(roomId);
        System.out.println("📦 Room fetched by ID:");
        System.out.println("   Subject: " + fetchedRoom.getSubject());
        System.out.println("   Topic: " + fetchedRoom.getTopic());
        System.out.println("   Created at: " + fetchedRoom.getCreatedAt());

        // 👥 TEST getRoomMembers
        List<RoomMemberResponse> members = roomService.getRoomMembers(roomId);
        System.out.println("👥 Room members:");
        for (RoomMemberResponse member : members) {
            System.out.println(" - " + member.getDisplayName() + " (" + member.getEmail() + ") - Role: " + member.getRole());
        }

        Thread.sleep(4000);

        // 🧽 Cleanup test data
        roomMessageService.deleteMessagesByRoomId(roomId);
        roomService.deleteRoomById(roomId);
        userRepository.deleteById(user.getId());
        userService.deleteUserByEmail("ai@studybuddy.com");
        System.out.println("🧽 Test data cleaned up.");
    }
}
