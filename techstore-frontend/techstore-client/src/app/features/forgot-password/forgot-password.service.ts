// forgot-password.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ApiResponse<T> {
  code?: number;
  message?: string;
  result?: T;
}

@Injectable({ providedIn: 'root' })
export class ForgotPasswordService {
  private readonly baseUrl = `${environment.userUrl}/customers`;

  constructor(private http: HttpClient) {}

  sendOtp(email: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/forgot-password`,
      null,
      { params: new HttpParams().set('email', email) }
    );
  }

  resetPassword(payload: {
    email: string;
    otp: string;
    newPassword: string;
    passwordConfirm: string;
  }): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/reset-password`,
      payload
    );
  }
}