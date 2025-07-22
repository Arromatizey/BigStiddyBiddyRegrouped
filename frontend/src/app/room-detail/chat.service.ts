import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RoomMessage, PostRoomMessageRequest } from '../shared/models/room.models';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getMessages(roomId: string): Observable<RoomMessage[]> {
    return this.http.get<RoomMessage[]>(`${this.baseUrl}/messages/room/${roomId}`);
  }

  sendMessage(roomId: string, request: PostRoomMessageRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/messages/room/${roomId}`, request);
  }

  sendMessageToAI(roomId: string, request: PostRoomMessageRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/messages/room/${roomId}/ai`, request);
  }

  // TODO: Implement WebSocket connection for real-time messages
  // connectToRoom(roomId: string): Observable<RoomMessage> {
  //   // WebSocket implementation using /chat endpoint from backend
  //   // const socket = new WebSocket(`ws://localhost:8080/chat`);
  //   // return webSocketSubject from socket connection
  // }
}
