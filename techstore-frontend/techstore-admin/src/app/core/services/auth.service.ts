import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { AuthenticationRequest } from '../../shared/models/authentication-request.model';
import { TokenService } from './token.service';
import { StaffService } from '../../features/staff/staff.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = environment.identityUrl + '/auth';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private staffService: StaffService
  ) {}

  // POST /auth/token/staff
  login(req: AuthenticationRequest): Observable<ApiResponse<{ token: string; id: number }>> {
    return this.http.post<ApiResponse<{ token: string; id: number }>>(
      `${this.baseUrl}/token/staff`,
      req
    ).pipe(
      tap(res => {
        const token = res.result?.token;
        const id    = res.result?.id;
        if (token) this.tokenService.setToken(token);
        if (id)    this.staffService.loadCurrentStaff(id).subscribe();
      })
    );
  }

  // POST /auth/refresh/staff
  refreshToken(): Observable<ApiResponse<{ token: string }>> {
    const token = this.tokenService.getToken();
    return this.http.post<ApiResponse<{ token: string }>>(
      `${this.baseUrl}/refresh/staff`,
      { token }
    ).pipe(
      tap(res => {
        const newToken = res.result?.token;
        if (newToken) this.tokenService.setToken(newToken);
      })
    );
  }

  // POST /auth/introspect
  introspect(): Observable<ApiResponse<{ valid: boolean }>> {
    const token = this.tokenService.getToken();
    return this.http.post<ApiResponse<{ valid: boolean }>>(
      `${this.baseUrl}/introspect`,
      { token }
    );
  }

  // POST /auth/logout
  logout(): Observable<ApiResponse<void>> {
    const token = this.tokenService.getToken();
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/logout`,
      { token }
    ).pipe(
      tap(() => {
        this.tokenService.removeToken();
        this.staffService.clearCurrentStaff();
      })
    );
  }
}