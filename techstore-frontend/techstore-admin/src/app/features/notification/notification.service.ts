import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { NotificationPageResponse, NotificationRequest, NotificationResponse } from './models/notification.model';


@Injectable({
  providedIn: 'root'
})
export class AdminNotificationService {

  private readonly baseUrl = environment.notificationUrl + '/posts';

  constructor(private http: HttpClient) {}

  // GET /posts?page=1&size=10
  getAll(page = 1, size = 10): Observable<ApiResponse<NotificationPageResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<ApiResponse<NotificationPageResponse>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // GET /posts/search?title=&fromDate=&toDate=&page=1&size=10
  search(params: {
    title?: string;
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<NotificationPageResponse>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 1)
      .set('size', params.size ?? 10);

    if (params.title)    httpParams = httpParams.set('title', params.title);
    if (params.fromDate) httpParams = httpParams.set('fromDate', params.fromDate);
    if (params.toDate)   httpParams = httpParams.set('toDate', params.toDate);

    return this.http.get<ApiResponse<NotificationPageResponse>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // POST /posts/create
  create(req: NotificationRequest): Observable<ApiResponse<NotificationResponse>> {
    return this.http.post<ApiResponse<NotificationResponse>>(
      `${this.baseUrl}/create`,
      req
    );
  }

  // PUT /posts/{postId}
  update(postId: string, req: NotificationRequest): Observable<ApiResponse<NotificationResponse>> {
    return this.http.put<ApiResponse<NotificationResponse>>(
      `${this.baseUrl}/${postId}`,
      req
    );
  }

  // DELETE /posts/{postId}
  delete(postId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${postId}`
    );
  }

  // PUT /posts/mark-as-read?postId=xxx
  markAsRead(postId: string): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('postId', postId);
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/mark-as-read`,
      null,
      { params }
    );
  }

  // PUT /posts/mark-all-as-read
  markAllAsRead(): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/mark-all-as-read`,
      null
    );
  }
}