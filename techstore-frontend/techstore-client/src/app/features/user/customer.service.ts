import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import {
  CustomerResponse,
  CustomerRegisterRequest,
  CustomerUpdateRequest
} from './models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private baseUrl = environment.userUrl + '/customers';

  // ─── Current user state ───────────────────────────────────────────
  private currentUserSubject = new BehaviorSubject<CustomerResponse | null>(null);

  /** Observable để các component subscribe theo dõi thay đổi */
  currentUser$ = this.currentUserSubject.asObservable();

  /** Lấy giá trị hiện tại (sync) — dùng khi không cần reactive */
  get currentUser(): CustomerResponse | null {
    return this.currentUserSubject.getValue();
  }

  constructor(private http: HttpClient) {}

  // ─── Load & cache current user ────────────────────────────────────

  /**
   * Gọi API lấy thông tin user đang đăng nhập và lưu vào state.
   * Gọi 1 lần sau khi login thành công hoặc khi app khởi động.
   */
  loadCurrentUser(id: number): Observable<ApiResponse<CustomerResponse>> {
    return this.http.get<ApiResponse<CustomerResponse>>(
      `${this.baseUrl}/${id}`
    ).pipe(
      tap(res => this.currentUserSubject.next(res.result ?? null))
    );
  }

  /** Xóa state khi logout */
  clearCurrentUser(): void {
    this.currentUserSubject.next(null);
  }

  // ─── CRUD ─────────────────────────────────────────────────────────

  // POST /customers/register
  register(req: CustomerRegisterRequest): Observable<ApiResponse<CustomerResponse>> {
    return this.http.post<ApiResponse<CustomerResponse>>(
      `${this.baseUrl}/register`,
      req
    );
  }

  // GET /customers/{id}
  getById(id: number): Observable<ApiResponse<CustomerResponse>> {
    return this.http.get<ApiResponse<CustomerResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  // GET /customers/all
  getAll(): Observable<ApiResponse<CustomerResponse[]>> {
    return this.http.get<ApiResponse<CustomerResponse[]>>(
      `${this.baseUrl}/all`
    );
  }

  // GET /customers/search
  search(params: {
    id?: number;
    email?: string;
    phone?: string;
    fullName?: string;
  }): Observable<ApiResponse<CustomerResponse[]>> {
    let httpParams = new HttpParams();
    if (params.id != null)  httpParams = httpParams.set('id', params.id);
    if (params.email)       httpParams = httpParams.set('email', params.email);
    if (params.phone)       httpParams = httpParams.set('phone', params.phone);
    if (params.fullName)    httpParams = httpParams.set('fullName', params.fullName);

    return this.http.get<ApiResponse<CustomerResponse[]>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // PATCH /customers/{id} — cập nhật state nếu là current user
  updateInfo(id: number, req: CustomerUpdateRequest): Observable<ApiResponse<CustomerResponse>> {
    return this.http.patch<ApiResponse<CustomerResponse>>(
      `${this.baseUrl}/${id}`,
      req
    ).pipe(
      tap(res => {
        if (this.currentUser?.id === id) {
          this.currentUserSubject.next(res.result ?? null);
        }
      })
    );
  }

  // POST /customers/{id}/avatar — cập nhật avatarUrl trong state
  uploadAvatar(id: number, file: File): Observable<ApiResponse<void>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/${id}/avatar`,
      formData
    ).pipe(
      tap(() => {
        // Reload để lấy avatarUrl mới từ server
        if (this.currentUser?.id === id) {
          this.loadCurrentUser(id).subscribe();
        }
      })
    );
  }

  // PUT /customers/{id}/password
  updatePassword(
    id: number,
    oldPassword: string,
    newPassword: string,
    passwordConfirm: string
  ): Observable<ApiResponse<void>> {
    const params = new HttpParams()
      .set('oldPassword', oldPassword)
      .set('newPassword', newPassword)
      .set('passwordConfirm', passwordConfirm);

    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/${id}/password`,
      null,
      { params }
    );
  }

  // PUT /customers/{id}/status
  updateStatus(id: number, status: string): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/${id}/status`,
      null,
      { params }
    );
  }
}