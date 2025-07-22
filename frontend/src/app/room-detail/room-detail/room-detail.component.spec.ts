import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

import { RoomDetailComponent } from './room-detail.component';
import { RoomsService } from '../../rooms/rooms.service';
import { AuthService } from '../../auth/auth.service';
import { WebSocketService } from '../../core/websocket.service';
import { Room } from '../../shared/models/room.models';

describe('RoomDetailComponent', () => {
  let component: RoomDetailComponent;
  let fixture: ComponentFixture<RoomDetailComponent>;
  let mockRoomsService: jasmine.SpyObj<RoomsService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;

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

  beforeEach(async () => {
    mockRoomsService = jasmine.createSpyObj('RoomsService', ['getRoomById', 'getRoomMembers']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', ['connect', 'disconnect']);

    await TestBed.configureTestingModule({
      declarations: [RoomDetailComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: RoomsService, useValue: mockRoomsService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: WebSocketService, useValue: mockWebSocketService },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '123' })
          }
        }
      ]
    })
    .compileComponents();
    
    mockRoomsService.getRoomById.and.returnValue(of(mockRoom));
    mockRoomsService.getRoomMembers.and.returnValue(of([]));
    mockAuthService.getCurrentUserId.and.returnValue('user123');
    
    fixture = TestBed.createComponent(RoomDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load room on init', () => {
    expect(mockRoomsService.getRoomById).toHaveBeenCalledWith('123');
    expect(component.room).toEqual(mockRoom);
  });

  it('should connect to WebSocket on init', () => {
    expect(mockWebSocketService.connect).toHaveBeenCalled();
  });

  it('should disconnect from WebSocket on destroy', () => {
    component.ngOnDestroy();
    expect(mockWebSocketService.disconnect).toHaveBeenCalled();
  });
});
