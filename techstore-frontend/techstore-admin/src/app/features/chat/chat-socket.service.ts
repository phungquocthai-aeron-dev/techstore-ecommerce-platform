import { Injectable, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatMessageResponse } from './models/chat.model';
import { io, Socket } from 'socket.io-client';

@Injectable({ providedIn: 'root' })
export class AdminChatSocketService implements OnDestroy {
  private socket!: Socket;

  private messageSubject = new Subject<ChatMessageResponse>();
  /** Stream tin nhắn realtime từ server */
  message$ = this.messageSubject.asObservable();

  connect(token: string, userType: string): void {
    if (this.socket?.connected) return;

    this.socket = io(environment.chatSocketUrl, {
      transports: ['websocket'],
      query: { token, userType },
    });

    this.socket.on('connect', () =>
      console.log('[AdminSocket] Connected:', this.socket.id)
    );

    // Server emit event "new_message" với payload ChatMessageResponse
    this.socket.on('new_message', (msg: ChatMessageResponse) => {
      console.log(msg)
      this.messageSubject.next(msg);
    });

    this.socket.on('disconnect', () =>
      console.log('[AdminSocket] Disconnected')
    );
  }

  disconnect(): void {
    this.socket?.disconnect();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}