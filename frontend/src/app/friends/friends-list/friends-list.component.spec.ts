import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef } from '@angular/core';
import { of } from 'rxjs';

import { FriendsListComponent } from './friends-list.component';
import { FriendsService } from '../../shared/services/friends.service';
import { User } from '../../shared/models/friends.models';

describe('FriendsListComponent', () => {
  let component: FriendsListComponent;
  let fixture: ComponentFixture<FriendsListComponent>;
  let mockFriendsService: jasmine.SpyObj<FriendsService>;

  const mockFriends: User[] = [
    {
      id: '1',
      email: 'friend1@test.com',
      displayName: 'Friend One',
      verified: true,
      createdAt: '2023-01-01',
      lastSeenAt: new Date().toISOString()
    },
    {
      id: '2',
      email: 'friend2@test.com',
      displayName: 'Friend Two',
      verified: true,
      createdAt: '2023-01-02',
      lastSeenAt: new Date(Date.now() - 10 * 60 * 1000).toISOString() // 10 minutes ago
    }
  ];

  beforeEach(async () => {
    mockFriendsService = jasmine.createSpyObj('FriendsService', [
      'getFriends',
      'isUserOnline'
    ], {
      friends$: of(mockFriends),
      onlineStatus$: of()
    });

    mockFriendsService.getFriends.and.returnValue(of(mockFriends));
    mockFriendsService.isUserOnline.and.returnValue(true);

    await TestBed.configureTestingModule({
      declarations: [FriendsListComponent],
      providers: [
        { provide: FriendsService, useValue: mockFriendsService },
        ChangeDetectorRef
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(FriendsListComponent);
    component = fixture.componentInstance;
    component.currentUserId = 'test-user-id';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load friends on init', () => {
    expect(mockFriendsService.getFriends).toHaveBeenCalledWith('test-user-id');
    expect(component.friends.length).toBe(2);
  });

  it('should generate user initials correctly', () => {
    expect(component.getUserInitials('John Doe')).toBe('JD');
    expect(component.getUserInitials('test@email.com')).toBe('TE');
    expect(component.getUserInitials()).toBe('?');
  });

  it('should check online status', () => {
    component.onlineStatuses.set('1', true);
    component.onlineStatuses.set('2', false);
    
    expect(component.isOnline('1')).toBe(true);
    expect(component.isOnline('2')).toBe(false);
    expect(component.isOnline('3')).toBe(false);
  });

  it('should generate last seen text correctly', () => {
    const user: User = {
      id: '1',
      email: 'test@test.com',
      verified: true,
      createdAt: '2023-01-01',
      lastSeenAt: new Date().toISOString()
    };

    component.onlineStatuses.set('1', true);
    expect(component.getLastSeenText(user)).toBe('En ligne');

    component.onlineStatuses.set('1', false);
    const result = component.getLastSeenText(user);
    expect(result).toContain('Il y a');
  });
}); 