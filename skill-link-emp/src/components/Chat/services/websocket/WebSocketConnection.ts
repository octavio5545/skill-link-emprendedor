import { Client } from '@stomp/stompjs';
import { API_CONFIG } from '../../config/api';

export class WebSocketConnection {
  private client: Client | null = null;
  private connected = false;
  private connectionPromise: Promise<void> | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  connect(): Promise<void> {
    if (this.connectionPromise) {
      return this.connectionPromise;
    }

    if (this.connected && this.client) {
      return Promise.resolve();
    }

    const wsUrl = API_CONFIG.WEBSOCKET.URL;
    this.connectionPromise = new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => {
          const ws = new WebSocket(wsUrl);
          ws.onopen = () => console.log('WebSocket nativo conectado a:', wsUrl);
          ws.onerror = (error) => console.error('WebSocket nativo error:', error);
          ws.onclose = (event) => console.log('WebSocket nativo cerrado:', event.code, event.reason);
          return ws;
        },
        
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        connectionTimeout: 15000,
        
        debug: (str) => {
          if (str.includes('ERROR') || str.includes('CONNECT')) {
            console.log('STOMP:', str);
          }
        },
        
        onConnect: (frame) => {
          this.connected = true;
          this.reconnectAttempts = 0;
          this.connectionPromise = null;
          resolve();
        },
        
        onStompError: (frame) => {
          this.connected = false;
          this.connectionPromise = null;
          reject(new Error(`STOMP error: ${frame.headers['message']}`));
        },
        
        onWebSocketError: (error) => {
          this.connected = false;
          this.connectionPromise = null;
          this.handleReconnection(reject, error);
        },
        
        onDisconnect: (frame) => {
          this.connected = false;
          this.connectionPromise = null;
        },
        
        onWebSocketClose: (event) => {
          this.connected = false;
          this.connectionPromise = null;
          
          if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.handleAutoReconnection();
          }
        }
      });

      try {
        this.client.activate();
      } catch (error) {
        this.connectionPromise = null;
        reject(error);
      }
    });

    return this.connectionPromise;
  }

  private handleReconnection(reject: (reason?: any) => void, error: any) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        this.connectionPromise = null;
        this.connect();
      }, 2000 * this.reconnectAttempts);
    } else {
      reject(error);
    }
  }

  private handleAutoReconnection() {
    this.reconnectAttempts++;
    setTimeout(() => {
      this.connectionPromise = null;
      this.connect();
    }, 3000);
  }

  disconnect(): void {
    if (this.client && this.connected) {
      this.client.deactivate();
      this.connected = false;
      this.connectionPromise = null;
      this.reconnectAttempts = 0;
    }
  }

  isConnected(): boolean {
    return this.connected && this.client !== null;
  }

  getClient(): Client | null {
    return this.client;
  }
}