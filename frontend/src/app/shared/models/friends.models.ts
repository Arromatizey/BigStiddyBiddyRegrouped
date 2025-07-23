export interface User {
  id: string;
  email: string;
  displayName?: string;
  avatarUrl?: string;
  verified: boolean;
  createdAt: string;
  lastSeenAt?: string;
}

export interface Friendship {
  requesterId: string;
  targetId: string;
  requester: User;
  target: User;
  status: FriendshipStatus;
  createdAt: string;
}

export enum FriendshipStatus {
  PENDING = 'pending',
  ACCEPTED = 'accepted'
}

export interface FriendRequest {
  userId: string;
  message?: string;
}

export interface FriendSearchResult extends User {
  isFriend: boolean;
  hasPendingRequest: boolean;
}

export interface OnlineStatus {
  userId: string;
  isOnline: boolean;
  lastSeenAt?: string;
}

// WebSocket event types for friends
export interface FriendStatusUpdate {
  type: 'FRIEND_REQUEST' | 'FRIEND_ACCEPTED' | 'FRIEND_REJECTED' | 'ONLINE_STATUS_CHANGED';
  userId: string;
  data: any;
} 