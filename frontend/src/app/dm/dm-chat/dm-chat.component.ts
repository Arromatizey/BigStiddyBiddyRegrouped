import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DmService } from '../../shared/services/dm.service';
import { FriendsService } from '../../shared/services/friends.service';
import { AuthService } from '../../auth/auth.service';
import { WebSocketService } from '../../core/websocket.service';
import { DmMessageResponse, DmMessageRequest, DmMessageEvent } from '../../shared/models/dm.models';
import { OnlineStatus } from '../../shared/models/friends.models';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-dm-chat',
  templateUrl: './dm-chat.component.html',
  styleUrls: ['./dm-chat.component.css']
})
export class DmChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  messages: DmMessageResponse[] = [];
  newMessage = '';
  loading = false;
  currentUserId = '';
  otherUserId = '';
  otherUserName = '';
  isOtherUserOnline = false;
  private wsSubscription: any;
  private destroy$ = new Subject<void>();

  constructor(
    private dmService: DmService,
    private friendsService: FriendsService,
    private authService: AuthService,
    private websocketService: WebSocketService,
    private route: ActivatedRoute,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId() || '';
    if (!this.currentUserId) {
      console.error('❌ No current user ID found');
      this.router.navigate(['/login']);
      return;
    }

    this.otherUserId = this.route.snapshot.paramMap.get('userId') || '';
    if (!this.otherUserId) {
      this.router.navigate(['/dm']);
      return;
    }

    console.log('👤 Current user ID:', this.currentUserId);
    console.log('👥 Other user ID:', this.otherUserId);

    this.loadConversation();
    this.setupWebSocket();
    this.subscribeToOnlineStatusUpdates();
    this.updateOnlineStatus();
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private subscribeToOnlineStatusUpdates(): void {
    this.friendsService.onlineStatus$.pipe(
      takeUntil(this.destroy$)
    ).subscribe((status: OnlineStatus) => {
      if (status.userId === this.otherUserId) {
        console.log('📡 Online status updated in DM chat:', status);
        this.isOtherUserOnline = status.isOnline;
      }
    });
  }

  private updateOnlineStatus(): void {
    // Get online status from friends service
    this.isOtherUserOnline = this.friendsService.isUserOnline(this.otherUserId);
  }

  loadConversation(): void {
    if (!this.currentUserId || !this.otherUserId) {
      console.error('❌ Cannot load conversation: missing user IDs');
      return;
    }

    this.loading = true;
    this.dmService.getConversation(this.currentUserId, this.otherUserId).subscribe({
      next: (messages) => {
        this.messages = messages;
        if (messages.length > 0) {
          this.otherUserName = messages[0].senderId === this.currentUserId ? 
            messages[0].receiverName : messages[0].senderName;
        }
        this.loading = false;
        console.log('✅ Conversation loaded between', this.currentUserId, 'and', this.otherUserId, ':', messages);
      },
      error: (error: any) => {
        console.error('❌ Error loading conversation:', error);
        this.loading = false;
      }
    });
  }

  setupWebSocket(): void {
    // TODO: Implement WebSocket subscription for DM messages
    // this.wsSubscription = this.websocketService.subscribe('/user/' + this.currentUserId + '/queue/dm', (message) => {
    //   const event: DmMessageEvent = JSON.parse(message.body);
    //   if (event.senderId === this.otherUserId || event.receiverId === this.otherUserId) {
    //     const messageResponse: DmMessageResponse = {
    //       id: event.messageId,
    //       senderId: event.senderId,
    //       senderName: event.senderName,
    //       receiverId: event.receiverId,
    //       receiverName: event.receiverName,
    //       message: event.message,
    //       createdAt: event.createdAt
    //     };
    //     this.messages.push(messageResponse);
    //   }
    // });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.currentUserId || !this.otherUserId) return;

    const request: DmMessageRequest = {
      senderId: this.currentUserId,
      receiverId: this.otherUserId,
      message: this.newMessage.trim()
    };

    this.dmService.sendMessage(request).subscribe({
      next: (response) => {
        this.messages.push(response);
        this.newMessage = '';
        console.log('✅ Message sent from', this.currentUserId, 'to', this.otherUserId, ':', response);
      },
      error: (error: any) => {
        console.error('❌ Error sending message:', error);
      }
    });
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  isOwnMessage(message: DmMessageResponse): boolean {
    return message.senderId === this.currentUserId;
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
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

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  trackByMessageId(index: number, message: DmMessageResponse): string {
    return message.id;
  }
} 