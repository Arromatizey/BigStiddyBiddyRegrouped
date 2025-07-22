import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ChatService } from './chat.service';
import { WebSocketService } from '../core/websocket.service';
import { environment } from '../../environments/environment';
import { RoomMessage, PostRoomMessageRequest } from '../shared/models/room.models';

describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;

  beforeEach(() => {
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', [
      'waitForConnection',
      'subscribe',
      'publish'
    ]);
    mockWebSocketService.waitForConnection.and.returnValue(Promise.resolve());

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ChatService,
        { provide: WebSocketService, useValue: mockWebSocketService }
      ]
    });
    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get messages', () => {
    const mockMessages: RoomMessage[] = [
      { id: '1', message: 'Test message', createdAt: '2023-01-01' }
    ];
    const roomId = '123';

    service.getMessages(roomId).subscribe(messages => {
      expect(messages).toEqual(mockMessages);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/messages/room/${roomId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockMessages);
  });

  it('should send message', () => {
    const roomId = '123';
    const request: PostRoomMessageRequest = {
      userId: 'user123',
      message: 'Hello world'
    };

    service.sendMessage(roomId, request).subscribe(() => {
      expect(true).toBe(true);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/messages/room/${roomId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });

  it('should send message to AI', () => {
    const roomId = '123';
    const request: PostRoomMessageRequest = {
      userId: 'user123',
      message: 'What is JavaScript?'
    };

    service.sendMessageToAI(roomId, request).subscribe(() => {
      expect(true).toBe(true);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/messages/room/${roomId}/ai`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });
});
