import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FriendsService } from '../shared/services/friends.service';
import { WebSocketService } from '../core/websocket.service';
import { AuthService } from '../auth/auth.service';
import { User, Friendship, FriendshipStatus } from '../shared/models/friends.models';
import { of } from 'rxjs';

describe('FriendsService', () => {
  let service: FriendsService;
  let httpMock: HttpTestingController;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockUser1: User = {
    id: '1',
    email: 'user1@example.com',
    displayName: 'User One',
    verified: true,
    createdAt: '2024-01-01',
    lastSeenAt: new Date().toISOString()
  };

  const mockUser2: User = {
    id: '2',
    email: 'user2@example.com',
    displayName: 'User Two',
    verified: true,
    createdAt: '2024-01-01'
  };

  const mockFriendship: Friendship = {
    requesterId: '1',
    targetId: '2',
    requester: mockUser1,
    target: mockUser2,
    status: FriendshipStatus.PENDING,
    createdAt: '2024-01-01'
  };

  beforeEach(() => {
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', ['connect', 'subscribe', 'waitForConnection']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);
    mockAuthService.getCurrentUserId.and.returnValue('1');
    mockWebSocketService.waitForConnection.and.returnValue(Promise.resolve());

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

  it('should get friends for a user', () => {
    const userId = '1';
    const mockFriends = [mockUser2];

    service.getFriends(userId).subscribe(friends => {
      expect(friends).toEqual(mockFriends);
    });

    const req = httpMock.expectOne(`${service['baseUrl']}/friends/users/${userId}/friends`);
    expect(req.request.method).toBe('GET');
    req.flush(mockFriends);
  });

  it('should get pending requests', () => {
    const userId = '1';
    const mockRequests = [mockFriendship];

    service.getPendingRequests(userId).subscribe(requests => {
      expect(requests).toEqual(mockRequests);
    });

    const req = httpMock.expectOne(`${service['baseUrl']}/friends/users/${userId}/pending-requests`);
    expect(req.request.method).toBe('GET');
    req.flush(mockRequests);
  });

  it('should search users', () => {
    const query = 'user';
    const mockResults = [mockUser2];

    service.searchUsers(query).subscribe(users => {
      expect(users).toEqual(mockResults);
    });

    const req = httpMock.expectOne(req => req.url.includes('/friends/search'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('query')).toBe(query);
    expect(req.request.params.get('currentUserId')).toBe('1');
    req.flush(mockResults);
  });

  it('should send friend request', () => {
    const targetUserId = '2';

    service.sendFriendRequest(targetUserId).subscribe(() => {
      expect(true).toBeTruthy();
    });

    const req = httpMock.expectOne(req => req.url.includes('/friends/request'));
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('from')).toBe('1');
    expect(req.request.params.get('to')).toBe(targetUserId);
    req.flush(null);
  });

  it('should accept friend request', () => {
    const fromUserId = '2';

    service.acceptFriendRequest(fromUserId).subscribe(() => {
      expect(true).toBeTruthy();
    });

    const req = httpMock.expectOne(req => req.url.includes('/friends/accept'));
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('from')).toBe(fromUserId);
    expect(req.request.params.get('to')).toBe('1');
    req.flush(null);
  });

  it('should check if user is online', () => {
    const fiveMinutesAgo = new Date(Date.now() - 4 * 60 * 1000).toISOString();
    const tenMinutesAgo = new Date(Date.now() - 10 * 60 * 1000).toISOString();

    expect(service.isUserOnline(fiveMinutesAgo)).toBeTruthy();
    expect(service.isUserOnline(tenMinutesAgo)).toBeFalsy();
    expect(service.isUserOnline(undefined)).toBeFalsy();
  });
}); 