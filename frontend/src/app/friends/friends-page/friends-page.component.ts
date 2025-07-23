import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FriendsService } from '../../shared/services/friends.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-friends-page',
  templateUrl: './friends-page.component.html',
  styleUrls: ['./friends-page.component.css']
})
export class FriendsPageComponent implements OnInit, OnDestroy {
  currentUserId: string | null = null;
  activeTab: 'friends' | 'requests' | 'add' = 'friends';
  
  private destroy$ = new Subject<void>();

  constructor(
    private friendsService: FriendsService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUserId();
    console.log('🏠 FriendsPage initializing for user:', this.currentUserId);

    if (!this.currentUserId) {
      console.warn('⚠️ No user ID found, user might not be logged in');
      // You might want to redirect to login page here
      return;
    }

    if (this.currentUserId) {
      // Initialize friends service
      this.friendsService.initializeForUser(this.currentUserId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.friendsService.disconnectFromFriendsUpdates();
  }

  setActiveTab(tab: 'friends' | 'requests' | 'add'): void {
    this.activeTab = tab;
  }
} 