import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { DmService } from '../../shared/services/dm.service';
import { FriendsService } from '../../shared/services/friends.service';
import { AuthService } from '../../auth/auth.service';
import { ConversationSummary } from '../../shared/models/dm.models';
import { OnlineStatus } from '../../shared/models/friends.models';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-conversations-list',
  templateUrl: './conversations-list.component.html',
  styleUrls: ['./conversations-list.component.css']
})
export class ConversationsListComponent implements OnInit, OnDestroy {
  conversations: ConversationSummary[] = [];
  loading = false;
  currentUserId = '';
  totalUnreadCount = 0;
  onlineStatuses: Map<string, boolean> = new Map();
  private destroy$ = new Subject<void>();

  constructor(
    private dmService: DmService,
    private friendsService: FriendsService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId() || '';
    if (!this.currentUserId) {
      console.error('❌ No current user ID found');
      this.router.navigate(['/login']);
      return;
    }
    
    console.log('👤 Current user ID:', this.currentUserId);
    this.loadConversations();
    this.subscribeToOnlineStatusUpdates();
    // Refresh conversations every 30 seconds
    setInterval(() => {
      this.loadConversations();
    }, 30000);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private subscribeToOnlineStatusUpdates(): void {
    this.friendsService.onlineStatus$.pipe(
      takeUntil(this.destroy$)
    ).subscribe((status: OnlineStatus) => {
      console.log('📡 Online status updated in DM:', status);
      this.onlineStatuses.set(status.userId, status.isOnline);
    });
  }

  loadConversations(): void {
    if (!this.currentUserId) {
      console.error('❌ Cannot load conversations: no current user ID');
      return;
    }

    this.loading = true;
    this.dmService.getUserConversations(this.currentUserId).subscribe({
      next: (conversations) => {
        this.conversations = conversations;
        this.calculateTotalUnreadCount();
        this.updateOnlineStatuses();
        this.loading = false;
        console.log('✅ Conversations loaded for user', this.currentUserId, ':', conversations);
      },
      error: (error) => {
        console.error('❌ Error loading conversations:', error);
        this.loading = false;
      }
    });
  }

  private calculateTotalUnreadCount(): void {
    this.totalUnreadCount = this.conversations.reduce((total, conv) => total + conv.unreadCount, 0);
  }

  private updateOnlineStatuses(): void {
    this.conversations.forEach(conversation => {
      const isOnline = this.friendsService.isUserOnline(conversation.lastMessageTime);
      this.onlineStatuses.set(conversation.otherUserId, isOnline);
    });
  }

  openConversation(userId: string): void {
    this.router.navigate(['/dm/chat', userId]);
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString();
    }
  }

  getRelativeTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

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

  getUserInitials(displayName: string): string {
    if (!displayName) return '?';
    return displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  isOnline(userId: string): boolean {
    return this.onlineStatuses.get(userId) || false;
  }

  trackByConversationId(index: number, conversation: ConversationSummary): string {
    return conversation.otherUserId;
  }
} 