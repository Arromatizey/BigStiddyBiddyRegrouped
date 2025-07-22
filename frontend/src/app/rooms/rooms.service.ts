import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Room, RoomDurationUpdateRequest } from '../shared/models/room.models';

@Injectable({
  providedIn: 'root'
})
export class RoomsService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Méthode utilitaire pour créer les headers
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    return headers;
  }

  createRoom(room: Room): Observable<string> {
    console.log('🏗️ Creating room with payload:', JSON.stringify(room, null, 2));
    console.log('🌐 Request URL:', `${this.baseUrl}/rooms`);
    console.log('🔧 Headers:', this.getHeaders());
    
    // Backend returns UUID as JSON string like "123e4567-e89b-12d3-a456-426614174000"
    // We need responseType: 'text' to handle the quoted UUID string
    return this.http.post(`${this.baseUrl}/rooms`, room, { 
      responseType: 'text',
      headers: this.getHeaders()
    }).pipe(
      tap(response => {
        console.log('✅ Room created successfully:', response);
      }),
      catchError(error => {
        console.error('❌ Error creating room:', error);
        console.error('🔍 Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url
        });
        throw error;
      })
    );
  }

  getRooms(): Observable<Room[]> {
    console.log('📋 Fetching rooms from:', `${this.baseUrl}/rooms/getAllRooms`);
    
    // Backend endpoint returns 404, using empty array as fallback
    return this.http.get<Room[]>(`${this.baseUrl}/rooms/getAllRooms`, {
      headers: this.getHeaders()
    }).pipe(
      tap(rooms => {
        console.log('✅ Rooms fetched successfully:', rooms);
      }),
      catchError(error => {
        console.warn('⚠️ getAllRooms endpoint not available:', error);
        console.error('🔍 Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url
        });
        // Return empty array to prevent app crash
        return of([]);
      })
    );
  }

  updateTheme(roomId: string, themeConfig: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/rooms/${roomId}/theme`, themeConfig, {
      headers: this.getHeaders()
    });
  }

  updateDurations(roomId: string, request: RoomDurationUpdateRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/rooms/${roomId}/durations`, request, {
      headers: this.getHeaders()
    });
  }

  joinRoom(roomId: string, userId: string): Observable<void> {
    console.log('🚪 Joining room:', roomId, 'with user:', userId);
    return this.http.post<void>(`${this.baseUrl}/rooms/${roomId}/join?userId=${userId}`, {}, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        console.log('✅ Successfully joined room:', roomId);
      }),
      catchError(error => {
        console.error('❌ Error joining room:', error);
        throw error;
      })
    );
  }

  // Timer controls
  startTimer(roomId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/timer/${roomId}/start`, {}, {
      headers: this.getHeaders()
    });
  }

  pauseTimer(roomId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/timer/${roomId}/pause`, {}, {
      headers: this.getHeaders()
    });
  }

  resumeTimer(roomId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/timer/${roomId}/resume`, {}, {
      headers: this.getHeaders()
    });
  }

  resetTimer(roomId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/timer/${roomId}/reset`, {}, {
      headers: this.getHeaders()
    });
  }

  getRoomById(roomId: string): Observable<Room> {
    return this.http.get<Room>(`${this.baseUrl}/rooms/${roomId}`, {
      headers: this.getHeaders()
    }).pipe(
      tap(room => {
        console.log('✅ Room fetched successfully:', room);
      }),
      catchError(error => {
        console.error('❌ Error fetching room:', error);
        throw error;
      })
    );
  }

  getRoomMembers(roomId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/rooms/${roomId}/members`, {
      headers: this.getHeaders()
    }).pipe(
      tap(members => {
        console.log('✅ Room members fetched successfully:', members);
      }),
      catchError(error => {
        console.error('❌ Error fetching room members:', error);
        throw error;
      })
    );
  }
}
