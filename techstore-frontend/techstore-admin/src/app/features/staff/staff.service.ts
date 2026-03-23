import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { StaffRequest, StaffResponse, StaffRoleUpdateRequest } from './models/staff.model';
import { PageResponse } from './models/page-response.model';


@Injectable({
  providedIn: 'root'
})
export class StaffService {

  private baseUrl = environment.userUrl + '/staffs';

  // ─── Current staff state ──────────────────────────────────────────
  private currentStaffSubject = new BehaviorSubject<StaffResponse | null>(null);

  /** Observable để các component subscribe theo dõi thay đổi */
  currentStaff$ = this.currentStaffSubject.asObservable();

  /** Lấy giá trị hiện tại (sync) — dùng khi không cần reactive */
  get currentStaff(): StaffResponse | null {
    return this.currentStaffSubject.getValue();
  }

  constructor(private http: HttpClient) {}

  // ─── Load & cache current staff ───────────────────────────────────

  /**
   * Gọi API lấy thông tin staff đang đăng nhập và lưu vào state.
   * Gọi 1 lần sau khi login thành công hoặc khi app khởi động.
   */
  loadCurrentStaff(id: number): Observable<ApiResponse<StaffResponse>> {
    return this.http.get<ApiResponse<StaffResponse>>(
      `${this.baseUrl}/${id}`
    ).pipe(
      tap(res => this.currentStaffSubject.next(res.result ?? null))
    );
  }

  /** Xóa state khi logout */
  clearCurrentStaff(): void {
    this.currentStaffSubject.next(null);
  }

  // ─── CRUD ─────────────────────────────────────────────────────────

  // POST /staffs
  create(req: StaffRequest): Observable<ApiResponse<StaffResponse>> {
    return this.http.post<ApiResponse<StaffResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // GET /staffs/{id}
  getById(id: number): Observable<ApiResponse<StaffResponse>> {
    return this.http.get<ApiResponse<StaffResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  getAllPaged(params: {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
} = {}): Observable<ApiResponse<PageResponse<StaffResponse>>> {

  const httpParams = new HttpParams()
    .set('page',    params.page    ?? 0)
    .set('size',    params.size    ?? 10)
    .set('sortBy',  params.sortBy  ?? 'id')
    .set('sortDir', params.sortDir ?? 'asc');

  return this.http.get<ApiResponse<PageResponse<StaffResponse>>>(
    `${this.baseUrl}/paged`,
    { params: httpParams }
  );
}

  // GET /staffs?id=&email=&phone=
  getOne(params: {
    id?: number;
    email?: string;
    phone?: string;
  }): Observable<ApiResponse<StaffResponse>> {
    let httpParams = new HttpParams();
    if (params.id != null) httpParams = httpParams.set('id', params.id);
    if (params.email)      httpParams = httpParams.set('email', params.email);
    if (params.phone)      httpParams = httpParams.set('phone', params.phone);

    return this.http.get<ApiResponse<StaffResponse>>(
      `${this.baseUrl}`,
      { params: httpParams }
    );
  }

  // GET /staffs/search
  search(params: {
    id?: number;
    email?: string;
    phone?: string;
    fullName?: string;
  }): Observable<ApiResponse<StaffResponse[]>> {
    let httpParams = new HttpParams();
    if (params.id != null)  httpParams = httpParams.set('id', params.id);
    if (params.email)       httpParams = httpParams.set('email', params.email);
    if (params.phone)       httpParams = httpParams.set('phone', params.phone);
    if (params.fullName)    httpParams = httpParams.set('fullName', params.fullName);

    return this.http.get<ApiResponse<StaffResponse[]>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // PATCH /staffs/{id} — cập nhật state nếu là current staff
  updateInfo(id: number, req: StaffRequest): Observable<ApiResponse<StaffResponse>> {
    return this.http.patch<ApiResponse<StaffResponse>>(
      `${this.baseUrl}/${id}`,
      req
    ).pipe(
      tap(res => {
        if (this.currentStaff?.id === id) {
          this.currentStaffSubject.next(res.result ?? null);
        }
      })
    );
  }

  // PUT /staffs/{id}/password
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

  // PUT /staffs/{id}/roles — cập nhật state nếu là current staff
  updateRoles(id: number, req: StaffRoleUpdateRequest): Observable<ApiResponse<StaffResponse>> {
    return this.http.put<ApiResponse<StaffResponse>>(
      `${this.baseUrl}/${id}/roles`,
      req
    ).pipe(
      tap(res => {
        if (this.currentStaff?.id === id) {
          this.currentStaffSubject.next(res.result ?? null);
        }
      })
    );
  }

  // PUT /staffs/{id}/status
  updateStatus(id: number, status: string): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/${id}/status`,
      null,
      { params }
    );
  }
}