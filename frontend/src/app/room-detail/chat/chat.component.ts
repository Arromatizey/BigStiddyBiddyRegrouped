import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef, NgZone, ChangeDetectionStrategy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ChatService } from '../chat.service';
import { AuthService } from '../../auth/auth.service';
import { Room, RoomMessage, PostRoomMessageRequest } from '../../shared/models/room.models';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() room!: Room;
  @Input() currentUserId!: string | null;
  @ViewChild('messagesContainer') private messagesContainer?: ElementRef;
  
  messages: RoomMessage[] = [];
  newMessage = '';
  loading = true;
  sendingMessage = false;
  shouldScrollToBottom = true;
  
  private destroy$ = new Subject<void>();

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    if (this.room?.id) {
      this.loadMessages();
      this.subscribeToNewMessages();
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.room?.id) {
      this.chatService.disconnectFromRoom(this.room.id);
    }
  }

  private loadMessages(): void {
    this.loading = true;
    this.chatService.getMessages(this.room.id!).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (messages) => {
        this.messages = messages.reverse(); // API returns messages in DESC order
        this.loading = false;
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.loading = false;
      }
    });
  }

  private subscribeToNewMessages(): void {
    // Connect to WebSocket for real-time messages
    this.chatService.connectToRoom(this.room.id!);
    
    // Subscribe to new messages
    this.chatService.messages$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(message => {
      console.log('💬 New message received in chat component:', message);
      this.ngZone.run(() => {
        this.messages.push(message);
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      });
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.currentUserId || this.sendingMessage) {
      return;
    }

    const message = this.newMessage.trim();
    const isAIMessage = message.startsWith('@ai ');
    
    const request: PostRoomMessageRequest = {
      userId: this.currentUserId,
      message: isAIMessage ? message.substring(0) : message
    };

    this.sendingMessage = true;
    this.newMessage = '';

    const sendObservable = isAIMessage 
      ? this.chatService.sendMessageToAI(this.room.id!, request)
      : this.chatService.sendMessage(this.room.id!, request);

    sendObservable.pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.sendingMessage = false;
        // Message will appear via WebSocket
      },
      error: (error) => {
        console.error('Error sending message:', error);
        this.sendingMessage = false;
        this.newMessage = request.message; // Restore message on error
      }
    });
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
      }
    } catch(err) {
      console.error('Could not scroll to bottom:', err);
    }
  }

  onScroll(): void {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      const atBottom = element.scrollHeight - element.scrollTop === element.clientHeight;
      this.shouldScrollToBottom = atBottom;
    }
  }

  formatMessageTime(createdAt?: string): string {
    if (!createdAt) return '';
    
    const date = new Date(createdAt);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    
    if (isToday) {
      return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString('fr-FR', { 
        day: '2-digit', 
        month: '2-digit',
        hour: '2-digit', 
        minute: '2-digit' 
      });
    }
  }

  isOwnMessage(message: RoomMessage): boolean {
    return message.user?.id === this.currentUserId;
  }

  isAIMessage(message: RoomMessage): boolean {
    return message.user?.email === 'ai@studybuddy.com';
  }
}
