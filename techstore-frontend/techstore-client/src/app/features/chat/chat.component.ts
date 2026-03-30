import { Component, OnInit, OnDestroy, AfterViewChecked, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ChatService } from './chat.service'; 
import { ChatSocketService } from './chat-socket.service';
import { TokenService } from '../../core/services/token.service';
import { StaffService } from '../user/staff.service';
import { StaffResponse } from '../user/models/staff.model'; 
import { ChatMessageResponse, ConversationResponse } from './models/chat.model';


@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messageContainer') private messageContainer!: ElementRef;

  // ─── State ────────────────────────────────────────────────────────────────
  conversations: ConversationResponse[] = [];
  activeConversation: ConversationResponse | null = null;
  messages: ChatMessageResponse[] = [];
  newMessage = '';

  // Staff list to start new chat
  staffList: StaffResponse[] = [];
  showNewChat = false;

  // Loading flags
  loadingConversations = true;
  loadingMessages = false;
  sending = false;

  // Chat widget open/close
  isOpen = false;

  private socketSub?: Subscription;
  private shouldScrollBottom = false;

  readonly USER_TYPE = 'EXTERNAL'; // customer

  constructor(
    private chatService: ChatService,
    private socketService: ChatSocketService,
    private tokenService: TokenService,
    private staffService: StaffService
  ) {}

  ngOnInit(): void {
    const token = this.tokenService.getToken();
    if (token) {
      this.socketService.connect(token, this.USER_TYPE);
      this.socketSub = this.socketService.message$.subscribe((msg) => {

        msg.me = msg.sender?.userId === this.tokenService.getUserId() + ""
                && msg.sender?.userType === this.USER_TYPE;

        if (msg.conversationId === this.activeConversation?.id) {
          // Avoid duplicate if we already optimistically added
          const exists = this.messages.some((m) => m.id === msg.id || msg.me);
          if (!exists) {
            this.messages.push(msg);
            this.shouldScrollBottom = true;
          }
        } else {
          // Mark conversation as having unread (badge)
          const conv = this.conversations.find(
            (c) => c.id === msg.conversationId
          );
          if (conv) (conv as any)._unread = ((conv as any)._unread || 0) + 1;
        }
      });
    }
    this.loadConversations();
    this.loadStaffList();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollBottom) {
      this.scrollToBottom();
      this.shouldScrollBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.socketSub?.unsubscribe();
    this.socketService.disconnect();
  }

  // ─── Conversations ────────────────────────────────────────────────────────

  loadConversations(): void {
    this.chatService.getMyConversations().subscribe({
      next: (res) => {
        this.conversations = res.result || [];
        this.loadingConversations = false;
      },
      error: () => (this.loadingConversations = false),
    });
  }

  selectConversation(conv: ConversationResponse): void {
    this.activeConversation = conv;
    (conv as any)._unread = 0;
    this.showNewChat = false;
    this.loadMessages(conv.id);
  }

  // ─── Messages ─────────────────────────────────────────────────────────────

  loadMessages(conversationId: string): void {
    this.loadingMessages = true;
    this.chatService.getMessages(conversationId, this.USER_TYPE).subscribe({
      next: (res) => {
        this.messages = res.result || [];
        this.loadingMessages = false;
        this.shouldScrollBottom = true;
      },
      error: () => (this.loadingMessages = false),
    });
  }

  sendMessage(): void {
    const text = this.newMessage.trim();
    if (!text || !this.activeConversation || this.sending) return;

    this.sending = true;
    const req = {
      conversationId: this.activeConversation.id,
      userType: this.USER_TYPE,
      message: text,
    };

    // Optimistic UI
    const optimistic: ChatMessageResponse = {
      id: 'temp-' + Date.now(),
      conversationId: this.activeConversation.id,
      me: true,
      message: text,
      sender: { userId: '', username: 'Bạn', avatar: '', userType: this.USER_TYPE },
      createdDate: new Date().toISOString(),
    };
    this.messages.push(optimistic);
    this.newMessage = '';
    this.shouldScrollBottom = true;

    this.chatService.sendMessage(req).subscribe({
      next: (res) => {
        // Replace optimistic message with real one
        const idx = this.messages.findIndex((m) => m.id === optimistic.id);
        if (idx !== -1 && res.result) this.messages[idx] = res.result;
        this.sending = false;
      },
      error: () => {
        // Remove optimistic on error
        this.messages = this.messages.filter((m) => m.id !== optimistic.id);
        this.sending = false;
      },
    });
  }

  onEnter(e: KeyboardEvent): void {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      this.sendMessage();
    }
  }

  // ─── New Conversation ─────────────────────────────────────────────────────

  loadStaffList(): void {
    this.staffService.getChatAvailableStaff().subscribe({
      next: (res) => (this.staffList = res.result || []),
    });
  }

  get filteredStaff(): StaffResponse[] {
    return this.staffList;
  }

  startChatWithStaff(staff: StaffResponse): void {
    this.chatService
      .createConversation({
        type: 'DIRECT',
        userCurrentType: this.USER_TYPE,
        userTargetType: 'INTERNAL',
        participantIds: [String(staff.id)],
      })
      .subscribe({
        next: (res) => {
          if (!res.result) return;
          const exists = this.conversations.find((c) => c.id === res.result!.id);
          if (!exists) this.conversations.unshift(res.result);
          this.selectConversation(res.result);
        },
      });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  getOtherParticipant(conv: ConversationResponse) {
    return conv.participants.find((p) => p.userType === 'INTERNAL');
  }

  getConvDisplayName(conv: ConversationResponse): string {
    return conv.conversationName || this.getOtherParticipant(conv)?.username || 'Nhân viên';
  }

  getConvAvatar(conv: ConversationResponse): string {
    return (
      conv.conversationAvatar ||
      this.getOtherParticipant(conv)?.avatar ||
      ''
    );
  }

  getUnread(conv: ConversationResponse): number {
    return (conv as any)._unread || 0;
  }

  formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }

  private scrollToBottom(): void {
    try {
      const el = this.messageContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }
}