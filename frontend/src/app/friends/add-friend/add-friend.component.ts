import { Component, Input, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FriendsService } from '../../shared/services/friends.service';
import { User } from '../../shared/models/friends.models';

@Component({
  selector: 'app-add-friend',
  templateUrl: './add-friend.component.html',
  styleUrls: ['./add-friend.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddFriendComponent implements OnInit, OnDestroy {
  @Input() currentUserId: string | null = null;

  searchQuery = '';
  searchResults: User[] = [];
  searching = false;
  searchError: string | null = null;
  sendingRequests: Set<string> = new Set();
  sentRequests: Set<string> = new Set();

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private friendsService: FriendsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('➕ AddFriendComponent initializing for user:', this.currentUserId);
    this.setupSearchDebounce();
    this.loadExistingRequestsStatus();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(query => {
      this.performSearch(query);
    });
  }

  private loadExistingRequestsStatus(): void {
    if (!this.currentUserId) return;

    // Load sent requests to mark users we've already sent requests to
    this.friendsService.getSentRequests(this.currentUserId).pipe(
      takeUntil(this.destroy$)
    ).subscribe(sentRequests => {
      this.sentRequests = new Set(sentRequests.map(req => req.targetId));
      this.cdr.detectChanges();
    });
  }

  onSearchInputChange(query: string): void {
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  private performSearch(query: string): void {
    if (!query.trim()) {
      this.searchResults = [];
      this.searching = false;
      this.searchError = null;
      this.cdr.detectChanges();
      return;
    }

    if (query.trim().length < 2) {
      this.searchError = 'Veuillez saisir au moins 2 caractères';
      this.searching = false;
      this.cdr.detectChanges();
      return;
    }

    // Check if user is logged in
    if (!this.currentUserId) {
      this.searchError = 'Vous devez être connecté pour rechercher des utilisateurs';
      this.searching = false;
      this.cdr.detectChanges();
      return;
    }

    console.log('🔍 Searching users with query:', query);
    console.log('🆔 Current user ID:', this.currentUserId);
    this.searching = true;
    this.searchError = null;

    this.friendsService.searchUsers(query.trim()).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (users) => {
        console.log('✅ Search results:', users);
        console.log('✅ Number of users found:', users.length);
        this.searchResults = users;
        this.searching = false;
        this.searchError = null;
        
        if (users.length === 0) {
          console.log('⚠️ No users found for query:', query);
        }
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error searching users:', error);
        console.error('❌ Error details:', error.message, error.status);
        this.searchError = 'Erreur lors de la recherche';
        this.searching = false;
        this.searchResults = [];
        this.cdr.detectChanges();
      }
    });
  }

  sendFriendRequest(user: User): void {
    if (this.sendingRequests.has(user.id) || this.sentRequests.has(user.id)) {
      return;
    }

    console.log('🤝 Sending friend request to:', user.displayName || user.email);
    this.sendingRequests.add(user.id);

    this.friendsService.sendFriendRequest(user.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        console.log('✅ Friend request sent successfully');
        this.sendingRequests.delete(user.id);
        this.sentRequests.add(user.id);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error sending friend request:', error);
        this.sendingRequests.delete(user.id);
        this.cdr.detectChanges();
        // Could show a toast or error message here
      }
    });
  }

  isSendingRequest(userId: string): boolean {
    return this.sendingRequests.has(userId);
  }

  hasRequestSent(userId: string): boolean {
    return this.sentRequests.has(userId);
  }

  getUserInitials(displayName?: string, email?: string): string {
    const name = displayName || email || '';
    if (!name) return '?';
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
    this.searching = false;
    this.searchError = null;
    this.cdr.detectChanges();
  }

  trackByUserId(index: number, user: User): string {
    return user.id;
  }
} 