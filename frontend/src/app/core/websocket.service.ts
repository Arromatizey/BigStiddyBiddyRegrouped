import { Injectable } from '@angular/core';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private connected$ = new BehaviorSubject<boolean>(false);
  private subscriptions: Map<string, StompSubscription> = new Map();

  constructor() {
    console.log('🔌 Initializing WebSocketService...');
    this.client = new Client({
      webSocketFactory: () => {
        console.log('🏭 Creating SockJS WebSocket factory...');
        return new SockJS('http://localhost:8080/chat') as any;
      },
      debug: (str) => {
        // Réduire les logs STOMP pour moins de bruit
        if (str.includes('>>> CONNECT') || str.includes('<<< CONNECTED') || str.includes('ERROR')) {
          console.log('STOMP: ' + str);
        }
      },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    this.client.onConnect = (frame) => {
      console.log('✅ WebSocket Connected successfully');
      this.connected$.next(true);
    };

    this.client.onDisconnect = () => {
      console.log('❌ WebSocket Disconnected');
      this.connected$.next(false);
    };

    this.client.onStompError = (frame) => {
      console.error('🔴 STOMP error:', frame.headers['message']);
      console.error('🔴 Error details:', frame.body);
    };

    this.client.onWebSocketError = (error) => {
      console.error('🔴 WebSocket error:', error);
    };
  }

  connect(): void {
    if (!this.client.active) {
      console.log('🔌 Activating WebSocket client...');
      this.client.activate();
    } else {
      console.log('🔌 WebSocket client already active');
    }
  }

  disconnect(): void {
    if (this.client.active) {
      this.subscriptions.forEach(sub => sub.unsubscribe());
      this.subscriptions.clear();
      this.client.deactivate();
    }
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  subscribe(destination: string, callback: (message: Message) => void): StompSubscription | null {
    if (!this.client.connected) {
      console.warn('⚠️ Cannot subscribe: WebSocket not connected to', destination);
      return null;
    }

    // Unsubscribe from existing subscription if any
    const existingSub = this.subscriptions.get(destination);
    if (existingSub) {
      console.log(`🔄 Unsubscribing from existing subscription: ${destination}`);
      existingSub.unsubscribe();
    }

    const subscription = this.client.subscribe(destination, callback);
    this.subscriptions.set(destination, subscription);
    console.log(`📡 Successfully subscribed to ${destination}`);
    return subscription;
  }

  publish(destination: string, body: any): void {
    if (!this.client.connected) {
      console.warn('⚠️ Cannot publish: WebSocket not connected');
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body)
    });
  }

  waitForConnection(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.client.connected) {
        resolve();
        return;
      }

      // Assurer que le client essaie de se connecter
      if (!this.client.active) {
        this.client.activate();
      }

      const checkConnection = setInterval(() => {
        if (this.client.connected) {
          clearInterval(checkConnection);
          resolve();
        }
      }, 100);

      // Timeout after 15 seconds
      setTimeout(() => {
        clearInterval(checkConnection);
        reject(new Error('WebSocket connection timeout after 15s'));
      }, 15000);
    });
  }
} 