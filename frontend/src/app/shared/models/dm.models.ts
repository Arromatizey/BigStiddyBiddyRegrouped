export interface DmMessageRequest {
  senderId: string;
  receiverId: string;
  message: string;
}

export interface DmMessageResponse {
  id: string;
  senderId: string;
  senderName: string;
  receiverId: string;
  receiverName: string;
  message: string;
  createdAt: string;
}

export interface ConversationSummary {
  otherUserId: string;
  otherUserName: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

export interface DmMessageEvent {
  type: string;
  messageId: string;
  senderId: string;
  senderName: string;
  receiverId: string;
  receiverName: string;
  message: string;
  createdAt: string;
} 