import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { WebSocketService } from '../../core/websocket.service';
import { AuthService } from '../../auth/auth.service';
import { Message } from '@stomp/stompjs';
import { 
  User, 
  Friendship, 
  FriendRequest, 
  FriendSearchResult, 
  OnlineStatus, 
  FriendStatusUpdate 
} from '../models/friends.models';

@Injectable({
  providedIn: 'root'
})
export class FriendsService {
  private baseUrl = environment.apiUrl;
  
  // Subjects for real-time updates
  private friendsSubject = new BehaviorSubject<User[]>([]);
  private pendingRequestsSubject = new BehaviorSubject<Friendship[]>([]);
  private onlineStatusSubject = new Subject<OnlineStatus>();
  private friendNotificationsSubject = new Subject<FriendStatusUpdate>();

  // Public observables
  public friends$ = this.friendsSubject.asObservable();
  public pendingRequests$ = this.pendingRequestsSubject.asObservable();
  public onlineStatus$ = this.onlineStatusSubject.asObservable();
  public friendNotifications$ = this.friendNotificationsSubject.asObservable();

  private currentUserId: string | null = null;

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService,
    private authService: AuthService
  ) {
    this.currentUserId = this.authService.getCurrentUserId();
    console.log('🤝 FriendsService initialized for user:', this.currentUserId);
    console.log('🔑 Token from authService:', this.authService.getToken());
  }

  // Méthode utilitaire pour créer les headers
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
      console.log('📋 Headers with token:', headers.get('Authorization'));
    } else {
      console.warn('⚠️ No token found in localStorage');
    }
    
    return headers;
  }

  // API Calls
  getFriends(userId: string): Observable<User[]> {
    console.log('🤝 Fetching friends for user:', userId);
    const url = `${this.baseUrl}/friends/users/${userId}/friends`;
    console.log('🌐 Request URL:', url);
    console.log('🔑 Headers:', this.getHeaders());
    
    return this.http.get<User[]>(url, {
      headers: this.getHeaders()
    }).pipe(
      tap(friends => {
        console.log('✅ Friends fetched successfully:', friends);
        this.friendsSubject.next(friends);
      }),
      catchError(error => {
        console.error('❌ Error fetching friends:', error);
        console.error('❌ Error status:', error.status);
        console.error('❌ Error message:', error.message);
        return of([]);
      })
    );
  }

  getPendingRequests(userId: string): Observable<Friendship[]> {
    console.log('🤝 Fetching pending requests for user:', userId);
    return this.http.get<Friendship[]>(`${this.baseUrl}/friends/users/${userId}/pending-requests`, {
      headers: this.getHeaders()
    }).pipe(
      tap(requests => {
        console.log('✅ Pending requests fetched successfully:', requests);
        this.pendingRequestsSubject.next(requests);
      }),
      catchError(error => {
        console.error('❌ Error fetching pending requests:', error);
        return of([]);
      })
    );
  }

  getSentRequests(userId: string): Observable<Friendship[]> {
    console.log('🤝 Fetching sent requests for user:', userId);
    return this.http.get<Friendship[]>(`${this.baseUrl}/friends/users/${userId}/sent-requests`, {
      headers: this.getHeaders()
    }).pipe(
      tap(requests => console.log('✅ Sent requests fetched successfully:', requests)),
      catchError(error => {
        console.error('❌ Error fetching sent requests:', error);
        return of([]);
      })
    );
  }

  searchUsers(query: string): Observable<User[]> {
    // Récupérer l'ID depuis AuthService à chaque appel
    this.currentUserId = this.authService.getCurrentUserId();
    
    console.log('🔍 Searching users with query:', query);
    console.log('🆔 Current user ID from AuthService:', this.currentUserId);
    
    if (!this.currentUserId) {
      console.warn('⚠️ No current user ID, returning empty array');
      return of([]);
    }
    
    const url = `${this.baseUrl}/friends/search`;
    const params = { query, currentUserId: this.currentUserId };
    console.log('🌐 Request URL:', url);
    console.log('📋 Request params:', params);
    
    return this.http.get<User[]>(url, {
      params: params,
      headers: this.getHeaders()
    }).pipe(
      tap(users => {
        console.log('✅ User search completed:', users);
        console.log('✅ Number of users found:', users.length);
      }),
      catchError(error => {
        console.error('❌ Error searching users:', error);
        console.error('❌ Error status:', error.status);
        console.error('❌ Error message:', error.message);
        return of([]);
      })
    );
  }

  sendFriendRequest(targetUserId: string): Observable<void> {
    if (!this.currentUserId) {
      return of();
    }

    console.log('🤝 Sending friend request from', this.currentUserId, 'to', targetUserId);
    return this.http.post<void>(`${this.baseUrl}/friends/request`, null, {
      params: { from: this.currentUserId, to: targetUserId },
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        console.log('✅ Friend request sent successfully');
        // WebSocket notification will be handled if implemented
      }),
      catchError(error => {
        console.error('❌ Error sending friend request:', error);
        throw error;
      })
    );
  }

  acceptFriendRequest(fromUserId: string): Observable<void> {
    if (!this.currentUserId) {
      return of();
    }

    console.log('✅ Accepting friend request from', fromUserId, 'to', this.currentUserId);
    return this.http.post<void>(`${this.baseUrl}/friends/accept`, null, {
      params: { from: fromUserId, to: this.currentUserId },
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        console.log('✅ Friend request accepted successfully');
        // Refresh friends and pending requests
        this.refreshFriendsData();
      }),
      catchError(error => {
        console.error('❌ Error accepting friend request:', error);
        throw error;
      })
    );
  }

  rejectFriendRequest(fromUserId: string): Observable<void> {
    if (!this.currentUserId) {
      return of();
    }

    console.log('❌ Rejecting friend request from', fromUserId, 'to', this.currentUserId);
    return this.http.post<void>(`${this.baseUrl}/friends/reject`, null, {
      params: { from: fromUserId, to: this.currentUserId },
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        console.log('✅ Friend request rejected successfully');
        // Refresh pending requests
        if (this.currentUserId) {
          this.getPendingRequests(this.currentUserId).subscribe();
        }
      }),
      catchError(error => {
        console.error('❌ Error rejecting friend request:', error);
        throw error;
      })
    );
  }

  updateLastSeen(userId: string): Observable<void> {
    console.log('⏰ Updating last seen for user:', userId);
    return this.http.post<void>(`${this.baseUrl}/users/${userId}/update-last-seen`, null, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => console.log('✅ Last seen updated successfully')),
      catchError(error => {
        console.error('❌ Error updating last seen:', error);
        return of();
      })
    );
  }

  // WebSocket Methods
  connectToFriendsUpdates(): void {
    if (!this.currentUserId) {
      console.warn('⚠️ Cannot connect to friends updates: no current user');
      return;
    }

    console.log('📡 Connecting to friends updates for user:', this.currentUserId);
    this.webSocketService.connect();
    
    this.webSocketService.waitForConnection().then(() => {
      console.log('📡 WebSocket connected, subscribing to friends updates...');
      
      // Subscribe to friend notifications
      this.webSocketService.subscribe(`/topic/users/${this.currentUserId}/friends`, (message: Message) => {
        console.log('📨 Received friend notification via WebSocket:', message.body);
        try {
          const notification = JSON.parse(message.body) as FriendStatusUpdate;
          this.friendNotificationsSubject.next(notification);
          
          // Handle different notification types
          switch (notification.type) {
            case 'FRIEND_ACCEPTED':
              this.refreshFriendsData();
              break;
            case 'FRIEND_REQUEST':
              if (this.currentUserId) {
                this.getPendingRequests(this.currentUserId).subscribe();
              }
              break;
            case 'ONLINE_STATUS_CHANGED':
              this.onlineStatusSubject.next(notification.data as OnlineStatus);
              break;
          }
        } catch (error) {
          console.error('❌ Error parsing friend notification:', error);
        }
      });

      // Subscribe to online status updates
      this.webSocketService.subscribe(`/topic/friends/status`, (message: Message) => {
        console.log('📨 Received online status update via WebSocket:', message.body);
        try {
          const status = JSON.parse(message.body) as OnlineStatus;
          this.onlineStatusSubject.next(status);
        } catch (error) {
          console.error('❌ Error parsing online status update:', error);
        }
      });
    }).catch(error => {
      console.error('❌ Failed to connect to friends WebSocket:', error);
    });
  }

  disconnectFromFriendsUpdates(): void {
    console.log('📡 Disconnecting from friends updates');
    // WebSocket service handles disconnection
  }

  // Helper Methods
  private refreshFriendsData(): void {
    if (this.currentUserId) {
      this.getFriends(this.currentUserId).subscribe();
      this.getPendingRequests(this.currentUserId).subscribe();
    }
  }

  // Update online status for current user
  heartbeat(): void {
    if (this.currentUserId) {
      this.updateLastSeen(this.currentUserId).subscribe();
    }
  }

  // Check if user is online based on last seen
  isUserOnline(lastSeenAt?: string): boolean {
    if (!lastSeenAt) return false;
    const lastSeen = new Date(lastSeenAt);
    const now = new Date();
    const fiveMinutesAgo = new Date(now.getTime() - 5 * 60 * 1000);
    return lastSeen > fiveMinutesAgo;
  }

  // Initialize service for a user
  initializeForUser(userId: string): void {
    this.currentUserId = userId;
    console.log('🤝 Initializing FriendsService for user:', userId);
    
    // Load initial data
    this.getFriends(userId).subscribe();
    this.getPendingRequests(userId).subscribe();
    
    // Connect to real-time updates
    this.connectToFriendsUpdates();
    
    // Start heartbeat
    this.heartbeat();
    
    // Set up periodic heartbeat (every 2 minutes)
    setInterval(() => this.heartbeat(), 2 * 60 * 1000);
  }
} 