// src/app/features/chatbot/chatbot.component.ts

import {
  Component, ElementRef, ViewChild,
  AfterViewChecked, OnInit, OnDestroy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ChatApiService } from './chat-api.service'; 
import { ChatSessionService } from './chat-session.service';
import { ChatMessage, ChatSocketResponse } from './models/chat.model';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit, AfterViewChecked, OnDestroy {

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isOpen = false;
  inputText = '';
  isTyping = false;
  isConnected = false;

  messages: ChatMessage[] = [
    {
      id: '0',
      role: 'bot',
      content: '👋 Xin chào! Tôi là Techie — trợ lý AI của TechStore.\nTôi có thể giúp bạn tìm sản phẩm, kiểm tra giá, tồn kho và nhiều hơn nữa!',
      timestamp: new Date()
    }
  ];

  suggestions = [
    '🔍 Tìm laptop gaming',
    '📦 Còn hàng iPhone 15?',
    '⚖️ So sánh Samsung vs iPhone',
    '🎟️ Mã giảm giá'
  ];

  private socketSub?: Subscription;
  private sessionId: string | null = null;
  private shouldScroll = false;

  constructor(
    private chatApi: ChatApiService,
    private sessionService: ChatSessionService,
  ) {}

  ngOnInit(): void {
    this.initSession();
  }

  // ─── Session + Socket ──────────────────────────────────────────────────

  private initSession(): void {
    // Lấy sessionId (extract từ sessionToken)
    const existingToken = this.sessionService.getSessionToken();

    if (existingToken) {
      this.sessionId = this.sessionService.getSessionId();
      this.connectSocket();
    } else {
      // Gọi server tạo session mới
      this.sessionService.getValidSessionToken().subscribe({
        next: () => {
          this.sessionId = this.sessionService.getSessionId();
          this.connectSocket();
        },
        error: () => {
          console.warn('[Chat] Session init failed — will use REST fallback');
          this.sessionId = crypto.randomUUID(); // fallback tạm
        }
      });
    }
  }

  private connectSocket(): void {
    this.chatApi.connectSocket(() => {
      this.isConnected = true;
    });

    // Lắng nghe response từ socket
    this.socketSub = this.chatApi.onSocketMessage$.subscribe(
      (res: ChatSocketResponse) => this.handleSocketResponse(res)
    );
  }

  private handleSocketResponse(res: ChatSocketResponse): void {
    if (res.status === 'TYPING') {
      this.isTyping = true;
      return;
    }

    this.isTyping = false;

    if (res.status === 'DONE' && res.message) {
      this.pushBotMessage(res.message, res);
    }

    if (res.status === 'ERROR') {
      this.pushBotMessage(
        res.message ?? '⚠️ Có lỗi xảy ra, vui lòng thử lại!',
        res
      );
    }
  }

  // ─── Gửi message ──────────────────────────────────────────────────────

  sendMessage(): void {
    const text = this.inputText.trim();
    if (!text || this.isTyping) return;

    // Hiển thị message của user
    this.messages.push({
      id: Date.now().toString(),
      role: 'user',
      content: text,
      timestamp: new Date()
    });

    this.inputText = '';
    this.shouldScroll = true;

    // Thử gửi qua WebSocket trước
    const sent = this.chatApi.sendSocket(text, this.sessionId ?? '');

    if (!sent) {
      // Fallback: gửi qua REST nếu socket chưa kết nối
      this.isTyping = true;
      this.chatApi.sendRest(text, this.sessionId ?? '').subscribe({
        next: res => {
          this.isTyping = false;
          if (res.result?.message) {
            this.pushBotMessage(res.result.message, res.result);
          }
        },
        error: () => {
          this.isTyping = false;
          this.pushBotMessage('⚠️ Không thể kết nối. Vui lòng thử lại sau!');
        }
      });
    }
  }

  sendSuggestion(text: string): void {
    // Bỏ emoji prefix nếu có (vd: "🔍 Tìm laptop gaming" → "Tìm laptop gaming")
    this.inputText = text.replace(/^[\p{Emoji}\s]+/u, '').trim();
    this.sendMessage();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  // ─── UI ────────────────────────────────────────────────────────────────

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) this.shouldScroll = true;
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────

  private pushBotMessage(content: string, extra?: any): void {
    this.messages.push({
      id: Date.now().toString(),
      role: 'bot',
      content,
      timestamp: new Date(),
      type: extra?.type,
      data: extra?.data,
      metadata: extra?.metadata
    });
    this.shouldScroll = true;
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }

  ngOnDestroy(): void {
    this.socketSub?.unsubscribe();
    this.chatApi.disconnectSocket();
  }
}