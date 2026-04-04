import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import {
  NotificationPageResponse
} from './models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private baseUrl = environment.notificationUrl + '/posts';

  // ─── Unread count state ────────────────────────────────────────────
  private unreadCount$ = new BehaviorSubject<number>(0);
  unreadCount = this.unreadCount$.asObservable();

  constructor(private http: HttpClient) {}

  // ─── Set unread count from outside ────────────────────────────────
  setUnreadCount(count: number): void {
    this.unreadCount$.next(count);
  }

  decrementUnread(amount = 1): void {
    const current = this.unreadCount$.value;
    this.unreadCount$.next(Math.max(0, current - amount));
  }

  resetUnread(): void {
    this.unreadCount$.next(0);
  }

  // ─── API ───────────────────────────────────────────────────────────

  /**
   * GET /posts/my-posts?page=1&size=10
   * Lấy danh sách thông báo của user hiện tại (phân trang)
   */
  getMyPosts(page = 1, size = 10): Observable<ApiResponse<NotificationPageResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<ApiResponse<NotificationPageResponse>>(
      `${this.baseUrl}/my-posts`,
      { params }
    );
  }

  /**
   * PUT /posts/mark-as-read?postId=xxx
   * Đánh dấu một thông báo là đã đọc
   */
  markAsRead(postId: string): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('postId', postId);
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/mark-as-read`,
      null,
      { params }
    ).pipe(
      tap(() => this.decrementUnread(1))
    );
  }

  /**
   * PUT /posts/mark-all-as-read
   * Đánh dấu tất cả thông báo là đã đọc
   */
  markAllAsRead(): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/mark-all-as-read`,
      null
    ).pipe(
      tap(() => this.resetUnread())
    );
  }
}