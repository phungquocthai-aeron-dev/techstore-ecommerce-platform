import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';

import { TokenService } from '../../services/token.service'; 
import { ChatSessionService } from './chat-session.service';
import { ChatHistoryItem, ChatRequest, ChatResponse, ChatSocketRequest, ChatSocketResponse } from './models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatApiService implements OnDestroy {

  private readonly restUrl = `${environment.chatBotUrl}/chat`;
  private stompClient: Client | null = null;
  private subscription: StompSubscription | null = null;

  // Stream các message nhận từ WebSocket
  private socketMessage$ = new Subject<ChatSocketResponse>();

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private sessionService: ChatSessionService
  ) {}

  // ─── REST ──────────────────────────────────────────────────────────────

  sendRest(message: string, sessionId: string): Observable<ApiResponse<ChatResponse>> {
    const body: ChatRequest = { message, sessionId };
    return this.http.post<ApiResponse<ChatResponse>>(this.restUrl, body);
  }

  getHistory(sessionId?: string): Observable<ApiResponse<ChatHistoryItem[]>> {
    const params: any = {};
    if (sessionId) params['sessionId'] = sessionId;
    return this.http.get<ApiResponse<ChatHistoryItem[]>>(
      `${this.restUrl}/history`, { params }
    );
  }

  // ─── WebSocket ─────────────────────────────────────────────────────────

  /** Observable lắng nghe response từ socket */
  get onSocketMessage$(): Observable<ChatSocketResponse> {
    return this.socketMessage$.asObservable();
  }

  /** Kết nối WebSocket — gọi 1 lần khi component khởi tạo */
  connectSocket(onConnected?: () => void): void {
    if (this.stompClient?.connected) return;

    const token = this.tokenService.getToken();
    const sessionToken = this.sessionService.getSessionToken();

    const headers: Record<string, string> = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (sessionToken) headers['sessionToken'] = sessionToken;

    this.stompClient = new Client({
        brokerURL: "ws://localhost:8888/techstore/api/v1/chatbot/ws/chat",
        connectHeaders: headers,
        reconnectDelay: 5000,

      onConnect: () => {
        console.log('[WS] Connected');
        this.subscribeToResponse();
        onConnected?.();
      },

      onDisconnect: () => {
        console.log('[WS] Disconnected');
      },

      onStompError: frame => {
        console.error('[WS] STOMP error:', frame.headers['message']);
      }
    });

    this.stompClient.activate();
  }

  /** Ngắt kết nối WebSocket */
  disconnectSocket(): void {
    this.subscription?.unsubscribe();
    this.stompClient?.deactivate();
    this.stompClient = null;
  }

  /** Gửi message qua WebSocket */
  sendSocket(message: string, sessionId: string): boolean {
    if (!this.stompClient?.connected) return false;

    const payload: ChatSocketRequest = {
      message,
      sessionId,
      token: this.tokenService.getToken()
    };

    this.stompClient.publish({
      destination: '/app/chat',
      body: JSON.stringify(payload)
    });

    return true;
  }

  get isSocketConnected(): boolean {
    return this.stompClient?.connected ?? false;
  }

  // ─── Private ───────────────────────────────────────────────────────────

  private subscribeToResponse(): void {
    if (!this.stompClient?.connected) return;

    this.subscription = this.stompClient.subscribe(
      '/user/queue/response',
      (frame: IMessage) => {
        try {
          const response: ChatSocketResponse = JSON.parse(frame.body);
          this.socketMessage$.next(response);
        } catch (e) {
          console.error('[WS] Parse error:', e);
        }
      }
    );
  }

  ngOnDestroy(): void {
    this.disconnectSocket();
    this.socketMessage$.complete();
  }
}