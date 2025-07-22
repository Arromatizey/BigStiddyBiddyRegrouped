import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoomsService } from '../../rooms/rooms.service';
import { AuthService } from '../../auth/auth.service';
import { WebSocketService } from '../../core/websocket.service';
import { Room } from '../../shared/models/room.models';

@Component({
  selector: 'app-room-detail',
  templateUrl: './room-detail.component.html',
  styleUrl: './room-detail.component.css'
})
export class RoomDetailComponent implements OnInit, OnDestroy {
  room: Room | null = null;
  roomMembers: any[] = [];
  currentUserId: string | null = null;
  loading = true;
  error: string | null = null;
  
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private roomsService: RoomsService,
    private authService: AuthService,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId();
    console.log('🏠 RoomDetailComponent initializing for user:', this.currentUserId);
    
    // Connect to WebSocket once
    console.log('🔌 Connecting to WebSocket...');
    this.webSocketService.connect();
    
    // Get room ID from route
    this.route.params.pipe(
      takeUntil(this.destroy$)
    ).subscribe(params => {
      const roomId = params['id'];
      console.log('🏠 Loading room:', roomId);
      if (roomId) {
        this.loadRoom(roomId);
      }
    });
  }

  ngOnDestroy(): void {
    console.log('🏠 RoomDetailComponent destroying...');
    this.destroy$.next();
    this.destroy$.complete();
    // Don't disconnect WebSocket here as other components might need it
    // this.webSocketService.disconnect();
  }

  private loadRoom(roomId: string): void {
    this.loading = true;
    this.error = null;

    // Load room details
    this.roomsService.getRoomById(roomId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (room) => {
        this.room = room;
        this.loadRoomMembers(roomId);
      },
      error: (error) => {
        console.error('Error loading room:', error);
        this.error = 'Impossible de charger la salle. Veuillez réessayer.';
        this.loading = false;
      }
    });
  }

  private loadRoomMembers(roomId: string): void {
    this.roomsService.getRoomMembers(roomId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (members) => {
        this.roomMembers = members;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading room members:', error);
        this.loading = false;
      }
    });
  }

  leaveRoom(): void {
    if (confirm('Êtes-vous sûr de vouloir quitter cette salle ?')) {
      this.router.navigate(['/rooms']);
    }
  }

  getRoomTheme(): string {
    if (!this.room?.themeConfig) return 'default';
    
    try {
      const config = JSON.parse(this.room.themeConfig);
      return config.theme || 'default';
    } catch {
      return 'default';
    }
  }
}
