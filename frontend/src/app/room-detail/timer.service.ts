import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, interval, Subscription } from 'rxjs';
import { WebSocketService } from '../core/websocket.service';
import { RoomsService } from '../rooms/rooms.service';
import { Message } from '@stomp/stompjs';

export interface TimerState {
  roomId: string;
  timerRunning: boolean;
  timerStartedAt: string | null;
  isOnBreak: boolean;
  focusDuration: number;
  breakDuration: number;
  elapsedSeconds: number;
  totalSeconds: number;
  phase: 'focus' | 'break';
}

@Injectable({
  providedIn: 'root'
})
export class TimerService {
  private timerState$ = new BehaviorSubject<TimerState | null>(null);
  private timerSubscription?: Subscription;
  
  constructor(
    private webSocketService: WebSocketService,
    private roomsService: RoomsService
  ) {}

  getTimerState(): Observable<TimerState | null> {
    return this.timerState$.asObservable();
  }

  connectToRoomTimer(roomId: string): void {
    this.webSocketService.waitForConnection().then(() => {
      this.webSocketService.subscribe(`/topic/rooms/${roomId}/timer`, (message: Message) => {
        const timerData = JSON.parse(message.body);
        this.updateTimerState(timerData);
      });
    }).catch(error => {
      console.error('Failed to connect to room timer:', error);
    });
  }

  initializeTimer(room: any): void {
    const initialState: TimerState = {
      roomId: room.id,
      timerRunning: room.timerRunning || false,
      timerStartedAt: room.timerStartedAt || null,
      isOnBreak: room.isOnBreak || false,
      focusDuration: room.focusDuration || 25,
      breakDuration: room.breakDuration || 5,
      elapsedSeconds: 0,
      totalSeconds: room.isOnBreak ? (room.breakDuration || 5) * 60 : (room.focusDuration || 25) * 60,
      phase: room.isOnBreak ? 'break' : 'focus'
    };

    if (initialState.timerRunning && initialState.timerStartedAt) {
      const startTime = new Date(initialState.timerStartedAt).getTime();
      const now = new Date().getTime();
      initialState.elapsedSeconds = Math.floor((now - startTime) / 1000);
    }

    this.timerState$.next(initialState);
    this.startLocalTimer();
  }

  private updateTimerState(timerData: any): void {
    const currentState = this.timerState$.value;
    if (!currentState) return;

    const updatedState: TimerState = {
      ...currentState,
      timerRunning: timerData.timerRunning,
      timerStartedAt: timerData.timerStartedAt,
      isOnBreak: timerData.isOnBreak,
      focusDuration: timerData.focusDuration || currentState.focusDuration,
      breakDuration: timerData.breakDuration || currentState.breakDuration,
      phase: timerData.isOnBreak ? 'break' : 'focus',
      totalSeconds: timerData.isOnBreak 
        ? (timerData.breakDuration || currentState.breakDuration) * 60 
        : (timerData.focusDuration || currentState.focusDuration) * 60
    };

    if (updatedState.timerRunning && updatedState.timerStartedAt) {
      const startTime = new Date(updatedState.timerStartedAt).getTime();
      const now = new Date().getTime();
      updatedState.elapsedSeconds = Math.floor((now - startTime) / 1000);
    } else {
      updatedState.elapsedSeconds = 0;
    }

    this.timerState$.next(updatedState);
    
    if (updatedState.timerRunning) {
      this.startLocalTimer();
    } else {
      this.stopLocalTimer();
    }
  }

  private startLocalTimer(): void {
    this.stopLocalTimer();
    
    this.timerSubscription = interval(1000).subscribe(() => {
      const state = this.timerState$.value;
      if (!state || !state.timerRunning) return;

      const updatedState = { ...state };
      
      if (state.timerStartedAt) {
        const startTime = new Date(state.timerStartedAt).getTime();
        const now = new Date().getTime();
        updatedState.elapsedSeconds = Math.floor((now - startTime) / 1000);
      }

      // Check if timer completed
      if (updatedState.elapsedSeconds >= updatedState.totalSeconds) {
        // Timer completed, switch phase or stop
        this.handleTimerComplete();
      } else {
        this.timerState$.next(updatedState);
      }
    });
  }

  private stopLocalTimer(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
      this.timerSubscription = undefined;
    }
  }

  private handleTimerComplete(): void {
    const state = this.timerState$.value;
    if (!state) return;

    // Play notification sound
    this.playNotificationSound();

    // Timer will be updated by WebSocket message from backend
  }

  private playNotificationSound(): void {
    const audio = new Audio();
    audio.src = 'data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSiMzPLThjMGHm7A7+OZURE';
    audio.play().catch(error => console.error('Error playing sound:', error));
  }

  // Control methods
  startTimer(roomId: string): void {
    this.roomsService.startTimer(roomId).subscribe({
      next: () => console.log('Timer started'),
      error: (error) => console.error('Error starting timer:', error)
    });
  }

  pauseTimer(roomId: string): void {
    this.roomsService.pauseTimer(roomId).subscribe({
      next: () => console.log('Timer paused'),
      error: (error) => console.error('Error pausing timer:', error)
    });
  }

  resumeTimer(roomId: string): void {
    this.roomsService.resumeTimer(roomId).subscribe({
      next: () => console.log('Timer resumed'),
      error: (error) => console.error('Error resuming timer:', error)
    });
  }

  resetTimer(roomId: string): void {
    this.roomsService.resetTimer(roomId).subscribe({
      next: () => console.log('Timer reset'),
      error: (error) => console.error('Error resetting timer:', error)
    });
  }

  cleanup(): void {
    this.stopLocalTimer();
    this.timerState$.next(null);
  }
} 