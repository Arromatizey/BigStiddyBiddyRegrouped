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
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/chat') as any,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('✅ Connected to WebSocket');
      this.connected$.next(true);
    };

    this.client.onDisconnect = () => {
      console.log('❌ Disconnected from WebSocket');
      this.connected$.next(false);
    };

    this.client.onStompError = (frame) => {
      console.error('🔴 STOMP error:', frame);
    };
  }

  connect(): void {
    if (!this.client.active) {
      this.client.activate();
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
      console.warn('⚠️ Cannot subscribe: WebSocket not connected');
      return null;
    }

    // Unsubscribe from existing subscription if any
    const existingSub = this.subscriptions.get(destination);
    if (existingSub) {
      existingSub.unsubscribe();
    }

    const subscription = this.client.subscribe(destination, callback);
    this.subscriptions.set(destination, subscription);
    console.log(`📡 Subscribed to ${destination}`);
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
      } else {
        const checkConnection = setInterval(() => {
          if (this.client.connected) {
            clearInterval(checkConnection);
            resolve();
          }
        }, 100);

        // Timeout after 10 seconds
        setTimeout(() => {
          clearInterval(checkConnection);
          reject(new Error('WebSocket connection timeout'));
        }, 10000);
      }
    });
  }
} 