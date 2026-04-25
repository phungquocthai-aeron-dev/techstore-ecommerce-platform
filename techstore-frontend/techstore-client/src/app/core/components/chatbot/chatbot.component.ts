// src/app/features/chatbot/chatbot.component.ts

import {
  Component, ElementRef, ViewChild,
  AfterViewChecked, OnInit, OnDestroy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';

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
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.initSession();
  }

  // ─── Session + Socket ──────────────────────────────────────────────────

  private initSession(): void {
    const existingToken = this.sessionService.getSessionToken();

    if (existingToken) {
      this.sessionId = this.sessionService.getSessionId();
      this.connectSocket();
    } else {
      this.sessionService.getValidSessionToken().subscribe({
        next: () => {
          this.sessionId = this.sessionService.getSessionId();
          this.connectSocket();
        },
        error: () => {
          console.warn('[Chat] Session init failed — will use REST fallback');
          this.sessionId = crypto.randomUUID();
        }
      });
    }
  }

  private connectSocket(): void {
    this.chatApi.connectSocket(() => {
      this.isConnected = true;
    });

    this.socketSub = this.chatApi.onSocketMessage$.subscribe(
      (res: ChatSocketResponse) => {
        console.warn(res)
        this.handleSocketResponse(res)
      }
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

    this.messages.push({
      id: Date.now().toString(),
      role: 'user',
      content: text,
      timestamp: new Date()
    });

    this.inputText = '';
    this.shouldScroll = true;

    const sent = this.chatApi.sendSocket(text, this.sessionId ?? '');

    if (!sent) {
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

  formatPrice(price: number): string {
    return price.toLocaleString('vi-VN') + ' đ';
  }

  // ─── Rich content helpers ─────────────────────────────────────────────

  /**
   * Trả về true nếu message có type PRODUCT_LIST và có data
   */
  isProductList(msg: ChatMessage): boolean {
    return msg.role === 'bot' && msg.type === 'PRODUCT_LIST' && Array.isArray(msg.data) && msg.data.length > 0;
  }

  /**
   * Trả về true nếu message có data là danh sách voucher (detect theo cấu trúc data[0].discountType)
   */
  isVoucherList(msg: ChatMessage): boolean {
    return msg.role === 'bot'
      && Array.isArray(msg.data)
      && msg.data.length > 0
      && msg.data[0]?.discountType !== undefined;
  }

  /**
   * Lấy tối đa 5 sản phẩm để hiển thị thẻ
   */
  getProductCards(msg: ChatMessage): any[] {
    return (msg.data ?? []).slice(0, 5);
  }

  /**
   * Lấy tối đa 5 voucher để hiển thị thẻ
   */
  getVoucherCards(msg: ChatMessage): any[] {
    return (msg.data ?? []).slice(0, 5);
  }

  /**
   * Có nhiều hơn 5 sản phẩm không?
   */
  hasMoreProducts(msg: ChatMessage): boolean {
    return Array.isArray(msg.data) && (msg.metadata?.total ?? msg.data.length) > 5;
  }

  /**
   * Keyword để link đến trang search
   */
  getSearchKeyword(msg: ChatMessage): string {
    return msg.metadata?.keyword ?? '';
  }

  getTotalProducts(msg: ChatMessage): number {
    return msg.metadata?.total ?? (msg.data?.length ?? 0);
  }

  navigateToProduct(id: number): void {
    this.router.navigate(['/product', id]);
  }

  navigateToSearch(keyword: string): void {
    this.router.navigate(['/search'], { queryParams: { keyword } });
  }

  navigateToCoupons(): void {
    this.router.navigate(['/promotions']);
  }

  /**
   * Parse markdown thành HTML cho phần text (không xử lý phần thẻ sản phẩm/voucher).
   * Chỉ dùng cho message TEXT thuần hoặc phần mô tả đầu của message có thẻ.
   */
  parseMarkdown(text: string): string {
    if (!text) return '';

    // Loại bỏ các link dạng 👉 http://... hoặc **http://...** (sẽ được xử lý riêng)
    let result = text;

    // Xử lý link markdown [text](url)
    result = result.replace(/\[([^\]]+)\]\((https?:\/\/[^\)]+)\)/g,
      '<a href="$2" target="_blank" rel="noopener" class="chat-link">$1</a>');

    // Xử lý 👉 url
    result = result.replace(/👉\s*(https?:\/\/\S+)/g, (_, url) => {
      const label = this.getLinkLabel(url);
      return `<a href="${url}" class="chat-btn-link" onclick="return false;" data-route="${url}">👉 ${label}</a>`;
    });

    // Bold: **text**
    result = result.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

    // Italic: *text*
    result = result.replace(/\*([^*]+)\*/g, '<em>$1</em>');

    // Dòng kẻ ---
    result = result.replace(/^---$/gm, '<hr class="chat-divider">');

    // Bullet list: dòng bắt đầu bằng •
    result = result.replace(/^•\s+(.+)$/gm, '<li>$1</li>');
    result = result.replace(/(<li>.*<\/li>)/gs, '<ul class="chat-list">$1</ul>');
    // Gộp các <ul> liền nhau
    result = result.replace(/<\/ul>\s*<ul class="chat-list">/g, '');

    // Newline → <br>
    result = result.replace(/\n/g, '<br>');

    // Xóa <br> thừa đầu/cuối
    result = result.replace(/^(<br>)+|(<br>)+$/g, '');

    return result;
  }

  /**
   * Trích xuất phần text giới thiệu (trước danh sách bullet) để hiển thị phía trên thẻ sản phẩm/voucher
   */
  getIntroText(msg: ChatMessage): string {
    if (!msg.content) return '';
    // Lấy dòng đầu tiên (trước dấu •)
    const firstBullet = msg.content.indexOf('\n•');
    const intro = firstBullet > 0 ? msg.content.substring(0, firstBullet) : msg.content.split('\n')[0];
    return this.parseMarkdown(intro.trim());
  }

  /**
   * Lấy phần text phía SAU danh sách bullet (footer info, link coupons, v.v.)
   */
  getFooterText(msg: ChatMessage): string {
    if (!msg.content) return '';
    const lines = msg.content.split('\n');
    // Tìm dòng cuối cùng có bullet
    let lastBulletIdx = -1;
    lines.forEach((l, i) => { if (l.trim().startsWith('•')) lastBulletIdx = i; });
    if (lastBulletIdx === -1) return '';
    const footerLines = lines.slice(lastBulletIdx + 1).join('\n').trim();
    if (!footerLines) return '';
    return this.parseMarkdown(footerLines);
  }

  private getLinkLabel(url: string): string {
    if (url.includes('/promotions')) return 'Xem tất cả mã giảm giá';
    if (url.includes('/search')) return 'Tìm kiếm sản phẩm';
    if (url.includes('/product')) return 'Xem sản phẩm';
    return 'Xem thêm';
  }

  getDiscountLabel(voucher: any): string {
    if (voucher.discountType === 'PERCENT') {
      return `-${voucher.discountValue}%`;
    }
    return `-${voucher.discountValue.toLocaleString('vi-VN')}đ`;
  }

  formatExpiry(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString('vi-VN');
    } catch {
      return dateStr;
    }
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