import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { ChatMessageRequest, ChatMessageResponse, ConversationRequest, ConversationResponse } from './models/chat.model';


@Injectable({ providedIn: 'root' })
export class ChatService {
  private baseUrl = environment.chatUrl;

  constructor(private http: HttpClient) {}

  // ─── Conversation ────────────────────────────────────────────────────────────

  createConversation(
    req: ConversationRequest
  ): Observable<ApiResponse<ConversationResponse>> {
    return this.http.post<ApiResponse<ConversationResponse>>(
      `${this.baseUrl}/conversations/create`,
      req
    );
  }

  getMyConversations(): Observable<ApiResponse<ConversationResponse[]>> {
    return this.http.get<ApiResponse<ConversationResponse[]>>(
      `${this.baseUrl}/conversations/my-conversations`
    );
  }

  // ─── Messages ────────────────────────────────────────────────────────────────

  sendMessage(
    req: ChatMessageRequest
  ): Observable<ApiResponse<ChatMessageResponse>> {
    return this.http.post<ApiResponse<ChatMessageResponse>>(
      `${this.baseUrl}/messages/create`,
      req
    );
  }

  getMessages(
    conversationId: string,
    userType: string
  ): Observable<ApiResponse<ChatMessageResponse[]>> {
    const params = new HttpParams()
      .set('conversationId', conversationId)
      .set('userType', userType);

    return this.http.get<ApiResponse<ChatMessageResponse[]>>(
      `${this.baseUrl}/messages`,
      { params }
    );
  }
}