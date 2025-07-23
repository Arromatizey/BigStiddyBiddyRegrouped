import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { FriendsService } from './friends.service';
import { WebSocketService } from '../../core/websocket.service';
import { AuthService } from '../../auth/auth.service';
import { environment } from '../../../environments/environment';
import { User, Friendship, FriendshipStatus } from '../models/friends.models';

describe('FriendsService', () => {
  let service: FriendsService;
  let httpMock: HttpTestingController;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockUser: User = {
    id: '1',
    email: 'test@test.com',
    displayName: 'Test User',
    verified: true,
    createdAt: '2023-01-01',
    lastSeenAt: new Date().toISOString()
  };

  const mockFriendship: Friendship = {
    requesterId: '1',
    targetId: '2',
    requester: mockUser,
    target: { ...mockUser, id: '2', email: 'friend@test.com' },
    status: FriendshipStatus.PENDING,
    createdAt: '2023-01-01'
  };

  beforeEach(() => {
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', [
      'connect',
      'waitForConnection',
      'subscribe'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);
    
    mockWebSocketService.waitForConnection.and.returnValue(Promise.resolve());
    mockAuthService.getCurrentUserId.and.returnValue('test-user-id');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        FriendsService,
        { provide: WebSocketService, useValue: mockWebSocketService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    });
    
    service = TestBed.inject(FriendsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get friends', () => {
    const mockFriends = [mockUser];
    const userId = 'test-user-id';

    service.getFriends(userId).subscribe(friends => {
      expect(friends).toEqual(mockFriends);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/friends/users/${userId}/friends`);
    expect(req.request.method).toBe('GET');
    req.flush(mockFriends);
  });

  it('should get pending requests', () => {
    const mockRequests = [mockFriendship];
    const userId = 'test-user-id';

    service.getPendingRequests(userId).subscribe(requests => {
      expect(requests).toEqual(mockRequests);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/friends/users/${userId}/pending-requests`);
    expect(req.request.method).toBe('GET');
    req.flush(mockRequests);
  });

  it('should send friend request', () => {
    const targetUserId = 'target-user-id';

    service.sendFriendRequest(targetUserId).subscribe();

    const req = httpMock.expectOne(
      `${environment.apiUrl}/friends/request?from=test-user-id&to=${targetUserId}`
    );
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should accept friend request', () => {
    const fromUserId = 'from-user-id';

    service.acceptFriendRequest(fromUserId).subscribe();

    const req = httpMock.expectOne(
      `${environment.apiUrl}/friends/accept?from=${fromUserId}&to=test-user-id`
    );
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should reject friend request', () => {
    const fromUserId = 'from-user-id';

    service.rejectFriendRequest(fromUserId).subscribe();

    const req = httpMock.expectOne(
      `${environment.apiUrl}/friends/reject?from=${fromUserId}&to=test-user-id`
    );
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should search users', () => {
    const query = 'test';
    const mockResults = [mockUser];

    service.searchUsers(query).subscribe(users => {
      expect(users).toEqual(mockResults);
    });

    const req = httpMock.expectOne(
      `${environment.apiUrl}/friends/search?query=${query}&currentUserId=test-user-id`
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockResults);
  });

  it('should check if user is online', () => {
    const now = new Date();
    const recentTime = new Date(now.getTime() - 2 * 60 * 1000).toISOString(); // 2 minutes ago
    const oldTime = new Date(now.getTime() - 10 * 60 * 1000).toISOString(); // 10 minutes ago

    expect(service.isUserOnline(recentTime)).toBe(true);
    expect(service.isUserOnline(oldTime)).toBe(false);
    expect(service.isUserOnline()).toBe(false);
  });

  it('should update last seen', () => {
    const userId = 'test-user-id';

    service.updateLastSeen(userId).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/users/${userId}/update-last-seen`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
}); 