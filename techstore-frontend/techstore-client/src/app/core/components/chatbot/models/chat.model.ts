export type MessageRole = 'user' | 'bot';
export type ResponseType = 'TEXT' | 'PRODUCT_LIST' | 'COMPARE' | 'STOCK';
export type SocketStatus = 'TYPING' | 'DONE' | 'ERROR';

// ─── Message hiển thị trên UI ─────────────────────────────────────────────
export interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  timestamp: Date;
  type?: ResponseType;
  data?: any;
  metadata?: any;
}

// ─── Request gửi lên REST ─────────────────────────────────────────────────
export interface ChatRequest {
  message: string;
  sessionId: string;
}

// ─── Response từ REST ─────────────────────────────────────────────────────
export interface ChatResponse {
  type: ResponseType;
  message: string;
  data?: any;
  metadata?: any;
}

// ─── WebSocket payload gửi lên ───────────────────────────────────────────
export interface ChatSocketRequest {
  message: string;
  sessionId: string;
  token: string | null;
}

// ─── WebSocket payload nhận về ───────────────────────────────────────────
export interface ChatSocketResponse {
  status: SocketStatus;
  type?: ResponseType;
  message?: string;
  data?: any;
  metadata?: any;
}

// ─── Session từ server ───────────────────────────────────────────────────
export interface SessionResponse {
  sessionId: string;
  sessionToken: string;
  expiresAt: number;
}

// ─── Lịch sử từ server ──────────────────────────────────────────────────
export interface ChatHistoryItem {
  id: string;
  conversationId: string;
  role: 'USER' | 'ASSISTANT';
  message: string;
  metadata?: any;
  createdAt: string;
}