package com.esgi.studyBuddy.service;

import com.esgi.studyBuddy.DTO.ConversationSummary;
import com.esgi.studyBuddy.DTO.DmMessageEvent;
import com.esgi.studyBuddy.DTO.DmMessageResponse;
import com.esgi.studyBuddy.model.DmMessage;
import com.esgi.studyBuddy.model.User;
import com.esgi.studyBuddy.repository.DmMessageRepository;
import com.esgi.studyBuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmMessageService {
    private final DmMessageRepository dmRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<DmMessageResponse> getConversation(UUID userA, UUID userB) {
        log.info("Getting conversation between users {} and {}", userA, userB);
        List<DmMessage> messages = dmRepository.findBySender_IdAndReceiver_IdOrReceiver_IdAndSender_IdOrderByCreatedAtAsc(userA, userB, userA, userB);
        
        log.info("Found {} messages in conversation between {} and {}", messages.size(), userA, userB);
        
        // Log each message to validate sender/receiver
        messages.forEach(msg -> {
            log.debug("Message {}: {} (sender) -> {} (receiver): {}", 
                    msg.getId(), 
                    msg.getSender().getDisplayName(), 
                    msg.getReceiver().getDisplayName(), 
                    msg.getMessage());
        });
        
        List<DmMessageResponse> responses = messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        log.debug("Returning {} message responses", responses.size());
        return responses;
    }

    public DmMessageResponse sendMessage(UUID senderId, UUID receiverId, String message) {
        log.info("Sending DM from {} to {}: {}", senderId, receiverId, message);
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        DmMessage dmMessage = DmMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .message(message)
                .build();

        DmMessage savedMessage = dmRepository.save(dmMessage);
        log.info("DM sent successfully with ID: {}", savedMessage.getId());
        
        // Send WebSocket event to receiver
        DmMessageEvent event = DmMessageEvent.builder()
                .messageId(savedMessage.getId())
                .senderId(sender.getId())
                .senderName(sender.getDisplayName())
                .receiverId(receiver.getId())
                .receiverName(receiver.getDisplayName())
                .message(savedMessage.getMessage())
                .createdAt(savedMessage.getCreatedAt())
                .build();
        
        // Send to specific user
        messagingTemplate.convertAndSend("/user/" + receiverId + "/queue/dm", event);
        log.info("WebSocket DM event sent to user: {}", receiverId);
        
        return convertToResponse(savedMessage);
    }

    public List<ConversationSummary> getUserConversations(UUID userId) {
        log.info("Getting conversations for user: {}", userId);
        
        // Get all messages where user is sender or receiver
        List<DmMessage> allMessages = dmRepository.findBySender_IdOrReceiver_IdOrderByCreatedAtDesc(userId, userId);
        log.info("Found {} total messages for user {}", allMessages.size(), userId);
        
        // Group by conversation partner and get latest message
        List<ConversationSummary> conversations = allMessages.stream()
                .collect(Collectors.groupingBy(msg -> {
                    // Determine the other user in the conversation
                    if (msg.getSender().getId().equals(userId)) {
                        return msg.getReceiver().getId(); // Current user is sender, other is receiver
                    } else {
                        return msg.getSender().getId(); // Current user is receiver, other is sender
                    }
                }))
                .values()
                .stream()
                .map(messages -> {
                    DmMessage latestMessage = messages.get(0); // Already sorted by createdAt desc
                    
                    // Determine the other user and their name
                    UUID otherUserId;
                    String otherUserName;
                    boolean isCurrentUserSender = latestMessage.getSender().getId().equals(userId);
                    
                    if (isCurrentUserSender) {
                        // Current user is sender, other is receiver
                        otherUserId = latestMessage.getReceiver().getId();
                        otherUserName = latestMessage.getReceiver().getDisplayName();
                        log.debug("Conversation: {} (sender) -> {} (receiver)", userId, otherUserId);
                    } else {
                        // Current user is receiver, other is sender
                        otherUserId = latestMessage.getSender().getId();
                        otherUserName = latestMessage.getSender().getDisplayName();
                        log.debug("Conversation: {} (sender) -> {} (receiver)", otherUserId, userId);
                    }
                    
                    // Count messages where current user is receiver (messages sent TO current user)
                    long messagesReceived = messages.stream()
                            .filter(msg -> msg.getReceiver().getId().equals(userId))
                            .count();
                    
                    // For now, consider all received messages as unread
                    // In a real app, you'd have a separate table for read status
                    int unreadCount = (int) messagesReceived;
                    
                    log.debug("Conversation with {}: {} total messages, {} received by user {} (unread: {})", 
                            otherUserName, messages.size(), messagesReceived, userId, unreadCount);
                    
                    return ConversationSummary.builder()
                            .otherUserId(otherUserId)
                            .otherUserName(otherUserName)
                            .lastMessage(latestMessage.getMessage())
                            .lastMessageTime(latestMessage.getCreatedAt())
                            .unreadCount(unreadCount)
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("Returning {} conversations for user {}", conversations.size(), userId);
        return conversations;
    }

    private DmMessageResponse convertToResponse(DmMessage message) {
        return DmMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getDisplayName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getDisplayName())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
