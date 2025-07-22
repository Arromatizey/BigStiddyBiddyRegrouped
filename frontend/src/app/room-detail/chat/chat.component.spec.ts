import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { ChatComponent } from './chat.component';
import { ChatService } from '../chat.service';
import { AuthService } from '../../auth/auth.service';
import { WebSocketService } from '../../core/websocket.service';
import { Room, RoomMessage } from '../../shared/models/room.models';

describe('ChatComponent', () => {
  let component: ChatComponent;
  let fixture: ComponentFixture<ChatComponent>;
  let mockChatService: jasmine.SpyObj<ChatService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockRoom: Room = {
    id: '123',
    subject: 'Test Room',
    level: 'Beginner',
    focusDuration: 25,
    breakDuration: 5,
    isActive: true,
    timerRunning: false,
    isOnBreak: false
  };

  const mockMessages: RoomMessage[] = [
    {
      id: '1',
      user: { id: 'user1', email: 'user1@test.com', verified: true, createdAt: '2023-01-01' },
      message: 'Hello',
      createdAt: '2023-01-01T10:00:00Z'
    }
  ];

  beforeEach(async () => {
    mockChatService = jasmine.createSpyObj('ChatService', [
      'getMessages', 
      'sendMessage', 
      'sendMessageToAI', 
      'connectToRoom', 
      'disconnectFromRoom'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);

    const mockWebSocketService = jasmine.createSpyObj('WebSocketService', ['connect']);

    await TestBed.configureTestingModule({
      declarations: [ChatComponent],
      imports: [FormsModule, HttpClientTestingModule],
      providers: [
        { provide: ChatService, useValue: mockChatService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: WebSocketService, useValue: mockWebSocketService }
      ]
    })
    .compileComponents();
    
    mockChatService.getMessages.and.returnValue(of(mockMessages));
    mockChatService.sendMessage.and.returnValue(of(void 0));
    mockChatService.messages$ = of();
    mockAuthService.getCurrentUserId.and.returnValue('user123');
    
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    component.room = mockRoom;
    component.currentUserId = 'user123';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load messages on init', () => {
    expect(mockChatService.getMessages).toHaveBeenCalledWith('123');
    expect(component.messages.length).toBe(1);
  });

  it('should send message', () => {
    component.newMessage = 'Test message';
    component.sendMessage();
    
    expect(mockChatService.sendMessage).toHaveBeenCalledWith('123', {
      userId: 'user123',
      message: 'Test message'
    });
    expect(component.newMessage).toBe('');
  });

  it('should send AI message when message starts with @ai', () => {
    component.newMessage = '@ai What is JavaScript?';
    component.sendMessage();
    
    expect(mockChatService.sendMessageToAI).toHaveBeenCalledWith('123', {
      userId: 'user123',
      message: 'What is JavaScript?'
    });
  });
});
