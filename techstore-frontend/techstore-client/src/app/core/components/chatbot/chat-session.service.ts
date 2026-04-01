import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { SessionResponse } from './models/chat.model';

const SESSION_TOKEN_KEY = 'chat_session_token';

@Injectable({ providedIn: 'root' })
export class ChatSessionService {

  private baseUrl = `${environment.chatBotUrl}/session`;

  constructor(private http: HttpClient) {}

  /**
   * Lấy sessionToken hợp lệ.
   * Nếu còn hạn → dùng lại từ localStorage.
   * Nếu hết hạn hoặc chưa có → gọi server tạo mới.
   */
  getValidSessionToken(): Observable<string> {
  const stored = localStorage.getItem(SESSION_TOKEN_KEY);

  if (stored && !this.isExpired(stored)) {
    return of(stored);
  }

  return this.createSession().pipe(
    tap(res => {
      const token = res.result!.sessionToken;
      localStorage.setItem(SESSION_TOKEN_KEY, token);
    }),
    map(res => res.result!.sessionToken)
  );
}

  /** Lấy sessionToken hiện tại (sync) */
  getSessionToken(): string | null {
    const token = localStorage.getItem(SESSION_TOKEN_KEY);
    return token && !this.isExpired(token) ? token : null;
  }

  /** Extract sessionId từ sessionToken (phần đầu trước dấu chấm) */
  getSessionId(): string | null {
    const token = this.getSessionToken();
    return token ? token.split('.')[0] : null;
  }

  /** Xóa session khi logout */
  clearSession(): void {
    localStorage.removeItem(SESSION_TOKEN_KEY);
  }

  private createSession(): Observable<ApiResponse<SessionResponse>> {
    return this.http.post<ApiResponse<SessionResponse>>(this.baseUrl, {});
  }

  private isExpired(sessionToken: string): boolean {
    try {
      const expiresAt = parseInt(sessionToken.split('.')[1]);
      return Date.now() / 1000 > expiresAt;
    } catch {
      return true;
    }
  }
}