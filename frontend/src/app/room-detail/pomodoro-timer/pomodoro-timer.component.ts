import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TimerService, TimerState } from '../timer.service';
import { Room } from '../../shared/models/room.models';

@Component({
  selector: 'app-pomodoro-timer',
  templateUrl: './pomodoro-timer.component.html',
  styleUrl: './pomodoro-timer.component.css'
})
export class PomodoroTimerComponent implements OnInit, OnDestroy {
  @Input() room!: Room;
  @Input() currentUserId!: string | null;
  
  timerState: TimerState | null = null;
  displayTime = '00:00';
  progress = 0;
  
  private destroy$ = new Subject<void>();

  constructor(private timerService: TimerService) {}

  ngOnInit(): void {
    if (this.room) {
      // Initialize timer with room data
      this.timerService.initializeTimer(this.room);
      
      // Connect to WebSocket timer updates
      this.timerService.connectToRoomTimer(this.room.id!);
      
      // Subscribe to timer state
      this.timerService.getTimerState().pipe(
        takeUntil(this.destroy$)
      ).subscribe(state => {
        this.timerState = state;
        this.updateDisplay();
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.timerService.cleanup();
  }

  private updateDisplay(): void {
    if (!this.timerState) {
      this.displayTime = '00:00';
      this.progress = 0;
      return;
    }

    const remainingSeconds = Math.max(0, this.timerState.totalSeconds - this.timerState.elapsedSeconds);
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    
    this.displayTime = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    this.progress = (this.timerState.elapsedSeconds / this.timerState.totalSeconds) * 100;
  }

  startTimer(): void {
    if (this.room?.id) {
      this.timerService.startTimer(this.room.id);
    }
  }

  pauseTimer(): void {
    if (this.room?.id) {
      this.timerService.pauseTimer(this.room.id);
    }
  }

  resumeTimer(): void {
    if (this.room?.id) {
      this.timerService.resumeTimer(this.room.id);
    }
  }

  resetTimer(): void {
    if (this.room?.id && confirm('Êtes-vous sûr de vouloir réinitialiser le minuteur ?')) {
      this.timerService.resetTimer(this.room.id);
    }
  }

  get canControl(): boolean {
    return !!this.currentUserId;
  }

  get isRunning(): boolean {
    return this.timerState?.timerRunning || false;
  }

  get phaseLabel(): string {
    if (!this.timerState) return 'Focus';
    return this.timerState.phase === 'focus' ? 'Focus' : 'Pause';
  }

  get phaseIcon(): string {
    if (!this.timerState) return '🎯';
    return this.timerState.phase === 'focus' ? '🎯' : '☕';
  }

  get progressColor(): string {
    if (!this.timerState) return '#4CAF50';
    return this.timerState.phase === 'focus' ? '#4CAF50' : '#2196F3';
  }
}
