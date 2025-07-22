import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { TimerService } from './timer.service';
import { WebSocketService } from '../core/websocket.service';
import { RoomsService } from '../rooms/rooms.service';

describe('TimerService', () => {
  let service: TimerService;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;
  let mockRoomsService: jasmine.SpyObj<RoomsService>;

  beforeEach(() => {
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', [
      'waitForConnection',
      'subscribe'
    ]);
    mockWebSocketService.waitForConnection.and.returnValue(Promise.resolve());

    mockRoomsService = jasmine.createSpyObj('RoomsService', [
      'startTimer',
      'pauseTimer',
      'resumeTimer',
      'resetTimer'
    ]);
    mockRoomsService.startTimer.and.returnValue(of(void 0));
    mockRoomsService.pauseTimer.and.returnValue(of(void 0));
    mockRoomsService.resumeTimer.and.returnValue(of(void 0));
    mockRoomsService.resetTimer.and.returnValue(of(void 0));

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        TimerService,
        { provide: WebSocketService, useValue: mockWebSocketService },
        { provide: RoomsService, useValue: mockRoomsService }
      ]
    });
    service = TestBed.inject(TimerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should initialize timer', () => {
    const room = {
      id: '123',
      timerRunning: false,
      timerStartedAt: null,
      isOnBreak: false,
      focusDuration: 25,
      breakDuration: 5
    };

    service.initializeTimer(room);
    
    service.getTimerState().subscribe(state => {
      expect(state).toBeTruthy();
      expect(state?.roomId).toBe('123');
      expect(state?.focusDuration).toBe(25);
      expect(state?.breakDuration).toBe(5);
    });
  });

  it('should start timer', () => {
    const roomId = '123';
    service.startTimer(roomId);
    expect(mockRoomsService.startTimer).toHaveBeenCalledWith(roomId);
  });

  it('should pause timer', () => {
    const roomId = '123';
    service.pauseTimer(roomId);
    expect(mockRoomsService.pauseTimer).toHaveBeenCalledWith(roomId);
  });

  it('should resume timer', () => {
    const roomId = '123';
    service.resumeTimer(roomId);
    expect(mockRoomsService.resumeTimer).toHaveBeenCalledWith(roomId);
  });

  it('should reset timer', () => {
    const roomId = '123';
    service.resetTimer(roomId);
    expect(mockRoomsService.resetTimer).toHaveBeenCalledWith(roomId);
  });
}); 