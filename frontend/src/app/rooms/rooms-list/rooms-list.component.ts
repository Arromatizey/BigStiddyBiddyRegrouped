import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { RoomsService } from '../rooms.service';
import { Room } from '../../shared/models/room.models';

@Component({
  selector: 'app-rooms-list',
  templateUrl: './rooms-list.component.html',
  styleUrls: ['./rooms-list.component.css']
})
export class RoomsListComponent implements OnInit {
  rooms: Room[] = [];
  loading = false;
  showCreateDialog = false;
  createRoomForm: FormGroup;

  constructor(
    private authService: AuthService,
    private roomsService: RoomsService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.createRoomForm = this.fb.group({
      subject: ['', Validators.required],
      level: ['', Validators.required],
      topic: [''],
      institution: [''],
      focusDuration: [25, [Validators.required, Validators.min(1)]],
      breakDuration: [5, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadRooms();
  }

  loadRooms(): void {
    this.loading = true;
    // Only load rooms from backend API - no hardcoded data
    this.roomsService.getRooms().subscribe({
      next: (rooms) => {
        this.rooms = rooms;
        this.loading = false;
        console.log('✅ Loaded rooms from backend:', rooms);
      },
      error: (error) => {
        console.error('❌ Error loading rooms:', error);
        this.rooms = []; // Empty array on error
        this.loading = false;
        
        // Log specific error types for debugging
        if (error.status === 401) {
          console.error('Authentication required to load rooms');
        } else if (error.status === 404) {
          console.error('Rooms endpoint not found');
        } else {
          console.error('Failed to load rooms from server');
        }
      }
    });
  }

  openCreateRoomDialog(): void {
    this.showCreateDialog = true;
  }

  hideCreateDialog(): void {
    this.showCreateDialog = false;
    this.createRoomForm.reset({
      focusDuration: 25,
      breakDuration: 5
    });
  }

  onCreateRoom(): void {
    if (this.createRoomForm.valid) {
      // Get current user ID from auth service
      const userId = this.authService.getCurrentUserId();
      console.log('🔍 Debug onCreateRoom:');
      console.log('🆔 User ID from auth service:', userId);
      console.log('🔑 Token from auth service:', this.authService.getToken());
      console.log('💾 LocalStorage userId:', localStorage.getItem('userId'));
      console.log('💾 LocalStorage token:', localStorage.getItem('token'));
      
      if (!userId) {
        console.error('❌ User not authenticated, cannot create room');
        console.error('🔍 Auth service state:');
        console.error('  - isAuthenticated():', this.authService.isAuthenticated());
        console.error('  - getCurrentUserId():', this.authService.getCurrentUserId());
        console.error('  - getToken():', this.authService.getToken());
        alert('Vous devez être connecté pour créer une salle');
        return;
      }

      // Create room payload - backend expects Room with owner having just ID
      const roomPayload = {
        subject: this.createRoomForm.value.subject,
        level: this.createRoomForm.value.level,
        topic: this.createRoomForm.value.topic || null,
        institution: this.createRoomForm.value.institution || null,
        focusDuration: this.createRoomForm.value.focusDuration,
        breakDuration: this.createRoomForm.value.breakDuration,
        owner: {
          id: userId
        },
        isActive: true,
        timerRunning: false,
        isOnBreak: false
      };
      
      console.log('📦 Creating room with payload:', JSON.stringify(roomPayload, null, 2));
      
      // Call the backend API to create the room
      this.roomsService.createRoom(roomPayload as Room).subscribe({
        next: (roomId) => {
          // Remove quotes from UUID if present
          const cleanRoomId = roomId.replace(/"/g, '');
          console.log('✅ Room created successfully with ID:', cleanRoomId);
          
          // Reload rooms list to get fresh data from backend
          this.loadRooms();
          this.hideCreateDialog();
          
          // Show success message
          alert('Salle créée avec succès !');
        },
        error: (error) => {
          console.error('❌ Error creating room:', error);
          console.error('Error details:', {
            status: error.status,
            message: error.message,
            error: error.error
          });
          
          // Show user-friendly error message
          if (error.status === 400) {
            alert('Données invalides. Vérifiez tous les champs.');
          } else if (error.status === 401) {
            alert('Session expirée. Veuillez vous reconnecter.');
            this.authService.logout();
          } else if (error.status === 0) {
            alert('Impossible de contacter le serveur. Vérifiez votre connexion.');
          } else {
            alert('Erreur lors de la création de la salle. Veuillez réessayer.');
          }
        }
      });
    }
  }

  joinRoom(roomId: string): void {
    const userId = this.authService.getCurrentUserId();
    console.log('🔍 Debug joinRoom:');
    console.log('🆔 User ID from auth service:', userId);
    console.log('🔑 Token from auth service:', this.authService.getToken());
    console.log('💾 LocalStorage userId:', localStorage.getItem('userId'));
    console.log('💾 LocalStorage token:', localStorage.getItem('token'));
    
    if (!userId) {
      console.error('❌ User not authenticated - userId is null!');
      console.error('🔍 Auth service state:');
      console.error('  - isAuthenticated():', this.authService.isAuthenticated());
      console.error('  - getCurrentUserId():', this.authService.getCurrentUserId());
      console.error('  - getToken():', this.authService.getToken());
      return;
    }

    console.log(`✅ Attempting to join room ${roomId} with user ${userId}`);
    
    this.roomsService.joinRoom(roomId, userId).subscribe({
      next: () => {
        console.log(`✅ Successfully joined room ${roomId}`);
        this.router.navigate(['/room', roomId]);
      },
      error: (error) => {
        console.error('❌ Error joining room:', error);
        // TODO: Show user-friendly error message (toast/snackbar)
        if (error.status === 404) {
          console.error('Room not found');
        } else if (error.status === 403) {
          console.error('Access denied to room');
        } else {
          console.error('Failed to join room. Please try again.');
        }
      }
    });
  }

  logout(): void {
    this.authService.logout(); // Navigation is handled by the service
  }

  // Méthode de test de connectivité backend
  testBackendConnectivity(): void {
    console.log('🔍 Testing backend connectivity...');
    
    const token = this.authService.getToken();
    console.log('🔑 Token for testing:', token ? 'Present' : 'Missing');
    
    // Test 1: Simple GET request sans auth
    console.log('🧪 Test 1: GET sans authentification...');
    fetch('http://localhost:8080/api/rooms/getAllRooms', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    .then(response => {
      console.log('✅ GET /rooms/getAllRooms - Status:', response.status);
      console.log('✅ GET /rooms/getAllRooms - Headers:', response.headers);
      return response.json();
    })
    .then(data => {
      console.log('✅ GET /rooms/getAllRooms - Data:', data);
    })
    .catch(error => {
      console.error('❌ GET /rooms/getAllRooms - Error:', error);
    });

    // Test 2: GET request avec auth
    if (token) {
      console.log('🧪 Test 2: GET avec authentification...');
      fetch('http://localhost:8080/api/rooms/getAllRooms', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      .then(response => {
        console.log('✅ GET (auth) /rooms/getAllRooms - Status:', response.status);
        console.log('✅ GET (auth) /rooms/getAllRooms - Headers:', response.headers);
        return response.json();
      })
      .then(data => {
        console.log('✅ GET (auth) /rooms/getAllRooms - Data:', data);
      })
      .catch(error => {
        console.error('❌ GET (auth) /rooms/getAllRooms - Error:', error);
      });
    }

    // Test 3: OPTIONS request (preflight)
    console.log('🧪 Test 3: OPTIONS preflight...');
    fetch('http://localhost:8080/api/rooms', {
      method: 'OPTIONS',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Access-Control-Request-Method': 'POST',
        'Access-Control-Request-Headers': 'Content-Type, Authorization'
      }
    })
    .then(response => {
      console.log('✅ OPTIONS /rooms - Status:', response.status);
      console.log('✅ OPTIONS /rooms - Headers:', response.headers);
      console.log('✅ OPTIONS /rooms - CORS Headers:');
      console.log('  - Access-Control-Allow-Origin:', response.headers.get('Access-Control-Allow-Origin'));
      console.log('  - Access-Control-Allow-Methods:', response.headers.get('Access-Control-Allow-Methods'));
      console.log('  - Access-Control-Allow-Headers:', response.headers.get('Access-Control-Allow-Headers'));
    })
    .catch(error => {
      console.error('❌ OPTIONS /rooms - Error:', error);
    });

    // Test 4: Test avec XMLHttpRequest (alternative à fetch)
    console.log('🧪 Test 4: XMLHttpRequest...');
    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'http://localhost:8080/api/rooms/getAllRooms', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.setRequestHeader('Accept', 'application/json');
    if (token) {
      xhr.setRequestHeader('Authorization', `Bearer ${token}`);
    }
    
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        console.log('✅ XMLHttpRequest - Status:', xhr.status);
        console.log('✅ XMLHttpRequest - Response:', xhr.responseText);
        console.log('✅ XMLHttpRequest - Headers:', xhr.getAllResponseHeaders());
      }
    };
    
    xhr.onerror = function() {
      console.error('❌ XMLHttpRequest - Error:', xhr.statusText);
    };
    
    xhr.send();
  }

  // Méthode de test spécifique pour la création de room
  testRoomCreation(): void {
    console.log('🧪 Testing room creation with different approaches...');
    
    const token = this.authService.getToken();
    const userId = this.authService.getCurrentUserId();
    
    if (!token || !userId) {
      console.error('❌ Missing token or userId for testing');
      return;
    }

    const testRoom = {
      subject: 'Test Room',
      level: 'Débutant',
      topic: null,
      institution: null,
      focusDuration: 25,
      breakDuration: 5,
      owner: { id: userId },
      isActive: true,
      timerRunning: false,
      isOnBreak: false
    };

    console.log('📋 Test room payload:', testRoom);

    // Test 1: Création avec fetch
    console.log('🧪 Test 1: Room creation with fetch...');
    fetch('http://localhost:8080/api/rooms', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(testRoom)
    })
    .then(response => {
      console.log('✅ POST /rooms (fetch) - Status:', response.status);
      console.log('✅ POST /rooms (fetch) - Headers:', response.headers);
      return response.text();
    })
    .then(data => {
      console.log('✅ POST /rooms (fetch) - Response:', data);
    })
    .catch(error => {
      console.error('❌ POST /rooms (fetch) - Error:', error);
    });

    // Test 2: Création avec XMLHttpRequest
    console.log('🧪 Test 2: Room creation with XMLHttpRequest...');
    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:8080/api/rooms', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.setRequestHeader('Accept', 'application/json');
    xhr.setRequestHeader('Authorization', `Bearer ${token}`);
    
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        console.log('✅ POST /rooms (XHR) - Status:', xhr.status);
        console.log('✅ POST /rooms (XHR) - Response:', xhr.responseText);
        console.log('✅ POST /rooms (XHR) - Headers:', xhr.getAllResponseHeaders());
      }
    };
    
    xhr.onerror = function() {
      console.error('❌ POST /rooms (XHR) - Error:', xhr.statusText);
    };
    
    xhr.send(JSON.stringify(testRoom));
  }
}
