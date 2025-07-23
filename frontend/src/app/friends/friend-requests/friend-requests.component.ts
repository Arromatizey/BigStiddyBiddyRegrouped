import { Component, Input, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FriendsService } from '../../shared/services/friends.service';
import { Friendship, FriendStatusUpdate } from '../../shared/models/friends.models';

@Component({
  selector: 'app-friend-requests',
  templateUrl: './friend-requests.component.html',
  styleUrls: ['./friend-requests.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FriendRequestsComponent implements OnInit, OnDestroy {
  @Input() currentUserId: string | null = null;

  pendingRequests: Friendship[] = [];
  sentRequests: Friendship[] = [];
  loading = true;
  error: string | null = null;
  processingRequests: Set<string> = new Set();

  private destroy$ = new Subject<void>();

  constructor(
    private friendsService: FriendsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('📬 FriendRequestsComponent initializing for user:', this.currentUserId);
    this.loadRequests();
    this.subscribeToUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadRequests(): void {
    if (!this.currentUserId) {
      this.loading = false;
      return;
    }

    this.loading = true;
    this.error = null;

    // Load pending requests (received)
    this.friendsService.getPendingRequests(this.currentUserId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (requests) => {
        console.log('✅ Pending requests loaded:', requests);
        this.pendingRequests = requests;
        this.loadSentRequests();
      },
      error: (error) => {
        console.error('❌ Error loading pending requests:', error);
        this.error = 'Erreur lors du chargement des demandes';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadSentRequests(): void {
    if (!this.currentUserId) {
      this.loading = false;
      return;
    }

    // Load sent requests
    this.friendsService.getSentRequests(this.currentUserId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (requests) => {
        console.log('✅ Sent requests loaded:', requests);
        this.sentRequests = requests;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error loading sent requests:', error);
        this.error = 'Erreur lors du chargement des demandes envoyées';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private subscribeToUpdates(): void {
    // Subscribe to pending requests updates
    this.friendsService.pendingRequests$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(requests => {
      console.log('📡 Pending requests updated via WebSocket:', requests);
      this.pendingRequests = requests;
      this.cdr.detectChanges();
    });

    // Subscribe to friend notifications
    this.friendsService.friendNotifications$.pipe(
      takeUntil(this.destroy$)
    ).subscribe((notification: FriendStatusUpdate) => {
      console.log('📡 Friend notification received:', notification);
      if (notification.type === 'FRIEND_REQUEST' || notification.type === 'FRIEND_ACCEPTED') {
        // Refresh requests data
        this.loadRequests();
      }
    });
  }

  acceptRequest(request: Friendship): void {
    if (this.processingRequests.has(request.requesterId)) {
      return;
    }

    console.log('✅ Accepting friend request from:', request.requester.displayName);
    this.processingRequests.add(request.requesterId);

    this.friendsService.acceptFriendRequest(request.requesterId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('✅ Friend request accepted successfully');
        this.processingRequests.delete(request.requesterId);
        // Remove from pending requests
        this.pendingRequests = this.pendingRequests.filter(r => r.requesterId !== request.requesterId);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error accepting friend request:', error);
        this.processingRequests.delete(request.requesterId);
        this.cdr.detectChanges();
        // Could show a toast or error message here
      }
    });
  }

  rejectRequest(request: Friendship): void {
    if (this.processingRequests.has(request.requesterId)) {
      return;
    }

    console.log('❌ Rejecting friend request from:', request.requester.displayName);
    this.processingRequests.add(request.requesterId);

    this.friendsService.rejectFriendRequest(request.requesterId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('✅ Friend request rejected successfully');
        this.processingRequests.delete(request.requesterId);
        // Remove from pending requests
        this.pendingRequests = this.pendingRequests.filter(r => r.requesterId !== request.requesterId);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error rejecting friend request:', error);
        this.processingRequests.delete(request.requesterId);
        this.cdr.detectChanges();
        // Could show a toast or error message here
      }
    });
  }

  isProcessing(requesterId: string): boolean {
    return this.processingRequests.has(requesterId);
  }

  getUserInitials(displayName?: string, email?: string): string {
    const name = displayName || email || '';
    if (!name) return '?';
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  getRequestAge(createdAt: string): string {
    const created = new Date(createdAt);
    const now = new Date();
    const diffMs = now.getTime() - created.getTime();
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

  refresh(): void {
    console.log('🔄 Refreshing friend requests');
    this.loadRequests();
  }

  trackByRequestId(index: number, request: Friendship): string {
    return request.requesterId + '-' + request.targetId;
  }
} 