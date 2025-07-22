import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { PomodoroTimerComponent } from './pomodoro-timer.component';
import { TimerService, TimerState } from '../timer.service';
import { WebSocketService } from '../../core/websocket.service';
import { RoomsService } from '../../rooms/rooms.service';
import { Room } from '../../shared/models/room.models';

describe('PomodoroTimerComponent', () => {
  let component: PomodoroTimerComponent;
  let fixture: ComponentFixture<PomodoroTimerComponent>;
  let mockTimerService: jasmine.SpyObj<TimerService>;

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

  const mockTimerState: TimerState = {
    roomId: '123',
    timerRunning: false,
    timerStartedAt: null,
    isOnBreak: false,
    focusDuration: 25,
    breakDuration: 5,
    elapsedSeconds: 0,
    totalSeconds: 1500,
    phase: 'focus'
  };

  beforeEach(async () => {
    mockTimerService = jasmine.createSpyObj('TimerService', [
      'initializeTimer',
      'connectToRoomTimer',
      'getTimerState',
      'startTimer',
      'pauseTimer',
      'resumeTimer',
      'resetTimer',
      'cleanup'
    ]);

    const mockWebSocketService = jasmine.createSpyObj('WebSocketService', ['connect']);
    const mockRoomsService = jasmine.createSpyObj('RoomsService', ['getRoomById']);

    await TestBed.configureTestingModule({
      declarations: [PomodoroTimerComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: TimerService, useValue: mockTimerService },
        { provide: WebSocketService, useValue: mockWebSocketService },
        { provide: RoomsService, useValue: mockRoomsService }
      ]
    })
    .compileComponents();
    
    mockTimerService.getTimerState.and.returnValue(of(mockTimerState));
    
    fixture = TestBed.createComponent(PomodoroTimerComponent);
    component = fixture.componentInstance;
    component.room = mockRoom;
    component.currentUserId = 'user123';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize timer on init', () => {
    expect(mockTimerService.initializeTimer).toHaveBeenCalledWith(mockRoom);
    expect(mockTimerService.connectToRoomTimer).toHaveBeenCalledWith('123');
  });

  it('should display correct time', () => {
    expect(component.displayTime).toBe('25:00');
  });

  it('should start timer when start button clicked', () => {
    component.startTimer();
    expect(mockTimerService.startTimer).toHaveBeenCalledWith('123');
  });

  it('should cleanup on destroy', () => {
    component.ngOnDestroy();
    expect(mockTimerService.cleanup).toHaveBeenCalled();
  });
});
