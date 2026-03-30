import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewChecked,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { AdminChatService } from './chat.service';
import { AdminChatSocketService } from './chat-socket.service';
import { StaffService } from '../staff/staff.service';
import { TokenService } from '../../core/services/token.service';
import { Customer } from '../customer/models/customer.model';
import { CustomerService } from '../customer/customer.service';
import { ChatMessageResponse, ConversationResponse } from './models/chat.model';

@Component({
  selector: 'app-admin-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class AdminChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('msgBox') private msgBox!: ElementRef;

  // ─── State ────────────────────────────────────────────────────────────────
  conversations: ConversationResponse[] = [];
  activeConv: ConversationResponse | null = null;
  messages: ChatMessageResponse[] = [];
  newMessage = '';

  // Sidebar tab: 'chats' | 'customers'
  sideTab: 'chats' | 'customers' = 'chats';

  // Customer list (to start new chat)
  customers: Customer[] = [];
  customerSearch = '';
  loadingCustomers = false;

  // Loading flags
  loadingConvs = true;
  loadingMsgs = false;
  sending = false;

  // Conv search
  convSearch = '';

  private socketSub?: Subscription;
  private needsScroll = false;

  /** Staff/Admin luôn là INTERNAL */
  readonly USER_TYPE = 'INTERNAL';

  constructor(
    private chatSvc: AdminChatService,
    private socketSvc: AdminChatSocketService,
    private staffSvc: StaffService,
    private tokenSvc: TokenService,
    private customerSvc: CustomerService,
  ) {}

  // ─── Lifecycle ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    const token = this.tokenSvc.getToken();
    if (token) {
      this.socketSvc.connect(token, this.USER_TYPE);
      this.socketSub = this.socketSvc.message$.subscribe((msg) => {
        msg.me = msg.sender?.userId === this.tokenSvc.getUserId() + ""
                && msg.sender?.userType === this.USER_TYPE;
        if (msg.conversationId === this.activeConv?.id) {
          const exists = this.messages.some((m) => m.id === msg.id || msg.me);
          if (!exists) {
            this.messages.push(msg);
            this.needsScroll = true;
          }
        } else {
          const conv = this.conversations.find((c) => c.id === msg.conversationId);
          if (conv) conv._unread = (conv._unread || 0) + 1;
        }
      });
    }
    this.loadConversations();
    this.loadCustomers();
  }

  ngAfterViewChecked(): void {
    if (this.needsScroll) {
      this.scrollBottom();
      this.needsScroll = false;
    }
  }

  ngOnDestroy(): void {
    this.socketSub?.unsubscribe();
    this.socketSvc.disconnect();
  }

  // ─── Conversations ────────────────────────────────────────────────────────

  loadConversations(): void {
    this.loadingConvs = true;
    this.chatSvc.getMyConversations().subscribe({
      next: (res) => {
        this.conversations = res.result || [];
        this.loadingConvs = false;
      },
      error: () => (this.loadingConvs = false),
    });
  }

  get filteredConvs(): ConversationResponse[] {
    const q = this.convSearch.toLowerCase().trim();
    if (!q) return this.conversations;
    return this.conversations.filter((c) =>
      this.getConvName(c).toLowerCase().includes(q)
    );
  }

  selectConv(conv: ConversationResponse): void {
    this.activeConv = conv;
    conv._unread = 0;
    this.loadMessages(conv.id);
    this.sideTab = 'chats';
  }

  // ─── Messages ─────────────────────────────────────────────────────────────

  loadMessages(convId: string): void {
    this.loadingMsgs = true;
    this.messages = [];
    this.chatSvc.getMessages(convId, this.USER_TYPE).subscribe({
      next: (res) => {
        this.messages = res.result || [];
        this.loadingMsgs = false;
        this.needsScroll = true;
      },
      error: () => (this.loadingMsgs = false),
    });
  }

  sendMessage(): void {
    const text = this.newMessage.trim();
    if (!text || !this.activeConv || this.sending) return;

    this.sending = true;

    // Optimistic
    const opt: ChatMessageResponse = {
      id: 'opt-' + Date.now(),
      conversationId: this.activeConv.id,
      me: true,
      message: text,
      sender: { userId: '', username: 'Bạn', avatar: '', userType: this.USER_TYPE },
      createdDate: new Date().toISOString(),
    };
    this.messages.push(opt);
    this.newMessage = '';
    this.needsScroll = true;

    this.chatSvc
      .sendMessage({ conversationId: this.activeConv.id, userType: this.USER_TYPE, message: text })
      .subscribe({
        next: (res) => {
          const idx = this.messages.findIndex((m) => m.id === opt.id);
          if (idx !== -1 && res.result) this.messages[idx] = res.result;
          this.sending = false;
        },
        error: () => {
          this.messages = this.messages.filter((m) => m.id !== opt.id);
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

  // ─── Customers (new chat) ─────────────────────────────────────────────────

  loadCustomers(): void {
    this.loadingCustomers = true;
    this.customerSvc.getAll().subscribe({
      next: (res) => {
        this.customers = res.result || [];
        this.loadingCustomers = false;
      },
      error: () => (this.loadingCustomers = false),
    });
  }

  get filteredCustomers(): Customer[] {
    const q = this.customerSearch.toLowerCase().trim();
    if (!q) return this.customers;
    return this.customers.filter(
      (c) =>
        c.fullName.toLowerCase().includes(q) ||
        c.email.toLowerCase().includes(q) ||
        (c.phone || '').includes(q)
    );
  }

  startChatWith(customer: Customer): void {
    this.chatSvc
      .createConversation({
        type: 'DIRECT',
        userCurrentType: this.USER_TYPE,
        userTargetType: 'EXTERNAL',
        participantIds: [String(customer.id)],
      })
      .subscribe({
        next: (res) => {
          if (!res.result) return;
          const exists = this.conversations.find((c) => c.id === res.result!.id);
          if (!exists) this.conversations.unshift(res.result);
          this.selectConv(res.result);
        },
      });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  getCustomerParticipant(conv: ConversationResponse) {
    return conv.participants.find((p) => p.userType === 'EXTERNAL');
  }

  getConvName(conv: ConversationResponse): string {
    return conv.conversationName || this.getCustomerParticipant(conv)?.username || 'Khách hàng';
  }

  getConvAvatar(conv: ConversationResponse): string {
    return conv.conversationAvatar || this.getCustomerParticipant(conv)?.avatar || '';
  }

  getInitial(name: string): string {
    if (!name?.trim()) return '?';
    const parts = name.trim().split(' ');
    return parts.length >= 2
      ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      : name[0].toUpperCase();
  }

  getAvatarBg(name: string): string {
    const colors = [
      'linear-gradient(135deg,#1d4ed8,#60a5fa)',
      'linear-gradient(135deg,#7c3aed,#a78bfa)',
      'linear-gradient(135deg,#0f766e,#34d399)',
      'linear-gradient(135deg,#b45309,#fbbf24)',
      'linear-gradient(135deg,#be185d,#f472b6)',
      'linear-gradient(135deg,#0e7490,#38bdf8)',
    ];
    let h = 0;
    for (let i = 0; i < (name?.length ?? 0); i++) h = name.charCodeAt(i) + ((h << 5) - h);
    return colors[Math.abs(h) % colors.length];
  }

  formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    const now = new Date();
    const isToday = d.toDateString() === now.toDateString();
    if (isToday) {
      return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
  }

  formatFullTime(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }

  totalUnread(): number {
    return this.conversations.reduce((sum, c) => sum + (c._unread || 0), 0);
  }

  private scrollBottom(): void {
    try {
      const el = this.msgBox?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }
}