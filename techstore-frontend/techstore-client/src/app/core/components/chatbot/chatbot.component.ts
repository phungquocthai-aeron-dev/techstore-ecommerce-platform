import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Message {
  id: string;
  role: 'user' | 'bot';
  content: string;
  timestamp: Date;
  typing?: boolean;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isOpen = false;
  inputText = '';
  isTyping = false;

  messages: Message[] = [
    {
      id: '1',
      role: 'bot',
      content: 'Xin chào! Tôi là trợ lý AI của NeonApp. Tôi có thể giúp gì cho bạn hôm nay? ✨',
      timestamp: new Date()
    }
  ];

  suggestions = ['Xem tính năng', 'Bảng giá', 'Hỗ trợ kỹ thuật'];

  private botReplies = [
    'Cảm ơn bạn đã liên hệ! Đội ngũ của chúng tôi sẽ sớm phản hồi.',
    'Tôi hiểu rồi! Bạn có thể cung cấp thêm thông tin không?',
    'Tất nhiên! Đây là một câu hỏi rất hay.',
    'Tính năng này đang được phát triển và sẽ sớm ra mắt nhé!',
    'Bạn có thể tham khảo tài liệu của chúng tôi tại trang Docs.',
  ];

  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  sendMessage(): void {
    const text = this.inputText.trim();
    if (!text) return;

    this.messages.push({
      id: Date.now().toString(),
      role: 'user',
      content: text,
      timestamp: new Date()
    });

    this.inputText = '';
    this.simulateBotReply();
  }

  sendSuggestion(text: string): void {
    this.inputText = text;
    this.sendMessage();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private simulateBotReply(): void {
    this.isTyping = true;
    setTimeout(() => {
      this.isTyping = false;
      const reply = this.botReplies[Math.floor(Math.random() * this.botReplies.length)];
      this.messages.push({
        id: Date.now().toString(),
        role: 'bot',
        content: reply,
        timestamp: new Date()
      });
    }, 1200 + Math.random() * 800);
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }
}