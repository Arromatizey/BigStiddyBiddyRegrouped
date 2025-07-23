import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DmMessageRequest, DmMessageResponse, ConversationSummary } from '../models/dm.models';

@Injectable({
  providedIn: 'root'
})
export class DmService {
  private apiUrl = `${environment.apiUrl}/dm`;

  constructor(private http: HttpClient) {}

  getUserConversations(userId: string): Observable<ConversationSummary[]> {
    return this.http.get<ConversationSummary[]>(`${this.apiUrl}/users/${userId}/conversations`);
  }

  getConversation(userA: string, userB: string): Observable<DmMessageResponse[]> {
    return this.http.get<DmMessageResponse[]>(`${this.apiUrl}/conversation`, {
      params: { userA, userB }
    });
  }

  sendMessage(request: DmMessageRequest): Observable<DmMessageResponse> {
    return this.http.post<DmMessageResponse>(`${this.apiUrl}/send`, request);
  }
} 