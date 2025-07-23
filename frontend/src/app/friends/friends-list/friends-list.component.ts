import { Component, Input, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FriendsService } from '../../shared/services/friends.service';
import { User, OnlineStatus } from '../../shared/models/friends.models';

@Component({
  selector: 'app-friends-list',
  templateUrl: './friends-list.component.html',
  styleUrls: ['./friends-list.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FriendsListComponent implements OnInit, OnDestroy {
  @Input() currentUserId: string | null = null;

  friends: User[] = [];
  onlineStatuses: Map<string, boolean> = new Map();
  loading = true;
  error: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private friendsService: FriendsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('👥 FriendsListComponent initializing for user:', this.currentUserId);
    this.loadFriends();
    this.subscribeToUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadFriends(): void {
    if (!this.currentUserId) {
      this.loading = false;
      return;
    }

    this.loading = true;
    this.friendsService.getFriends(this.currentUserId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (friends) => {
        console.log('✅ Friends loaded:', friends);
        this.friends = friends;
        this.updateOnlineStatuses();
        this.loading = false;
        this.error = null;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error loading friends:', error);
        this.error = 'Erreur lors du chargement des amis';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private subscribeToUpdates(): void {
    // Subscribe to friends list updates
    this.friendsService.friends$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(friends => {
      console.log('📡 Friends list updated via WebSocket:', friends);
      this.friends = friends;
      this.updateOnlineStatuses();
      this.cdr.detectChanges();
    });

    // Subscribe to online status updates
    this.friendsService.onlineStatus$.pipe(
      takeUntil(this.destroy$)
    ).subscribe((status: OnlineStatus) => {
      console.log('📡 Online status updated:', status);
      this.onlineStatuses.set(status.userId, status.isOnline);
      this.cdr.detectChanges();
    });
  }

  private updateOnlineStatuses(): void {
    this.friends.forEach(friend => {
      const isOnline = this.friendsService.isUserOnline(friend.lastSeenAt);
      this.onlineStatuses.set(friend.id, isOnline);
    });
  }

  isOnline(userId: string): boolean {
    return this.onlineStatuses.get(userId) || false;
  }

  getLastSeenText(user: User): string {
    if (this.isOnline(user.id)) {
      return 'En ligne';
    }
    
    if (!user.lastSeenAt) {
      return 'Jamais vu';
    }

    const lastSeen = new Date(user.lastSeenAt);
    const now = new Date();
    const diffMs = now.getTime() - lastSeen.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMinutes < 60) {
      return `Il y a ${diffMinutes} min`;
    } else if (diffHours < 24) {
      return `Il y a ${diffHours}h`;
    } else {
      return `Il y a ${diffDays}j`;
    }
  }

  getUserInitials(displayName?: string): string {
    if (!displayName) return '?';
    return displayName
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  refresh(): void {
    console.log('🔄 Refreshing friends list');
    this.loadFriends();
  }

  trackByFriendId(index: number, friend: User): string {
    return friend.id;
  }
} 