import { Component, Input, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
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
    private router: Router,
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
      return 'Jamais connecté';
    }

    const lastSeen = new Date(user.lastSeenAt);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - lastSeen.getTime()) / (1000 * 60));

    if (diffInMinutes < 1) {
      return 'À l\'instant';
    } else if (diffInMinutes < 60) {
      return `Il y a ${diffInMinutes} min`;
    } else if (diffInMinutes < 1440) {
      const hours = Math.floor(diffInMinutes / 60);
      return `Il y a ${hours}h`;
    } else {
      const days = Math.floor(diffInMinutes / 1440);
      return `Il y a ${days}j`;
    }
  }

  getUserInitials(displayName?: string): string {
    if (!displayName) return '?';
    return displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  refresh(): void {
    this.loadFriends();
  }

  openChat(friendId: string): void {
    console.log('💬 Opening chat with friend:', friendId);
    this.router.navigate(['/dm/chat', friendId]);
  }

  trackByFriendId(index: number, friend: User): string {
    return friend.id;
  }
} 