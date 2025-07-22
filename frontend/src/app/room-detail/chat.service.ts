import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { RoomMessage, PostRoomMessageRequest } from '../shared/models/room.models';
import { WebSocketService } from '../core/websocket.service';
import { Message } from '@stomp/stompjs';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private baseUrl = environment.apiUrl;
  private messagesSubject = new Subject<RoomMessage>();
  public messages$ = this.messagesSubject.asObservable();

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService
  ) {}

  getMessages(roomId: string): Observable<RoomMessage[]> {
    return this.http.get<RoomMessage[]>(`${this.baseUrl}/messages/room/${roomId}`);
  }

  sendMessage(roomId: string, request: PostRoomMessageRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/messages/room/${roomId}`, request);
  }

  sendMessageToAI(roomId: string, request: PostRoomMessageRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/messages/room/${roomId}/ai`, request);
  }

  connectToRoom(roomId: string): void {
    console.log('📡 ChatService connecting to room:', roomId);
    // First, ensure WebSocket is connected
    this.webSocketService.connect();
    
    // Subscribe to room messages
    this.webSocketService.waitForConnection().then(() => {
      console.log('📡 ChatService WebSocket connected, subscribing to messages...');
      const subscription = this.webSocketService.subscribe(`/topic/rooms/${roomId}/messages`, (message: Message) => {
        console.log('📨 Received chat message via WebSocket:', message.body);
        try {
          const roomMessage = JSON.parse(message.body) as RoomMessage;
          console.log('📨 Parsed message:', roomMessage);
          this.messagesSubject.next(roomMessage);
        } catch (error) {
          console.error('❌ Error parsing WebSocket message:', error, message.body);
        }
      });
      
      if (subscription) {
        console.log('✅ Successfully subscribed to room messages');
      } else {
        console.error('❌ Failed to create subscription');
      }
    }).catch(error => {
      console.error('❌ Failed to connect to room chat:', error);
    });
  }

  disconnectFromRoom(roomId: string): void {
    // WebSocket service handles unsubscription automatically
  }

  publishMessage(roomId: string, message: RoomMessage): void {
    this.webSocketService.publish(`/app/rooms/${roomId}/messages`, message);
  }
}
