export interface User {
  id: string;
  email: string;
  displayName?: string;
  avatarUrl?: string;
  verified: boolean;
  createdAt: string;
}

export interface Room {
  id?: string;
  owner?: User;
  subject: string;
  level: string;
  topic?: string;
  institution?: string;
  focusDuration: number;
  breakDuration: number;
  themeConfig?: string;
  isActive: boolean;
  createdAt?: string;
  timerRunning: boolean;
  timerStartedAt?: string;
  isOnBreak: boolean;
}

export interface RoomMessage {
  id?: string;
  room?: Room;
  user?: User;
  message: string;
  createdAt?: string;
}

export interface PostRoomMessageRequest {
  userId: string;
  message: string;
}

export interface RoomDurationUpdateRequest {
  focusDuration?: number;
  breakDuration?: number;
} 