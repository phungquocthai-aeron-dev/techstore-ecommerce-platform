import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';

import { ApiResponse } from '../../shared/models/api-response.model';
import { AuthenticationRequest } from '../../shared/models/authentication-request.model';
import { TokenService } from './token.service';
import { CustomerRegisterRequest } from '../../features/customer/models/customer-register.model';
import { Customer } from '../../features/customer/models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = `${environment.identityUrl}/auth`;

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {}

  // Register
    register(request: CustomerRegisterRequest): Observable<ApiResponse<Customer>> {
      return this.http.post<ApiResponse<Customer>>(
        `${environment.userUrl}/customers/register`,
        request
      );
    }

  // Login customer
  loginCustomer(request: AuthenticationRequest): Observable<ApiResponse<{ token: string }>> {
    return this.http.post<ApiResponse<{ token: string }>>(
      `${this.baseUrl}/token/customer`,
      request
    ).pipe(
      tap(res => {
        const token = res.result?.token;
        if (token) {
          this.tokenService.setToken(token);
        }
      })
    );
  }

  // Refresh customer
  refreshCustomerToken(): Observable<ApiResponse<{ token: string }>> {
    const token = this.tokenService.getToken();
    return this.http.post<ApiResponse<{ token: string }>>(
      `${this.baseUrl}/refresh/customer`,
      { token }
    ).pipe(
      tap(res => {
        const newToken = res.result?.token;
        if (newToken) {
          this.tokenService.setToken(newToken);
        }
      })
    );
  }

  // Introspect
  introspect(): Observable<ApiResponse<any>> {
    const token = this.tokenService.getToken();
    return this.http.post<ApiResponse<any>>(
      `${this.baseUrl}/introspect`,
      { token }
    );
  }

  // Logout
  logout(): Observable<ApiResponse<void>> {
    const token = this.tokenService.getToken();
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/logout`,
      { token }
    ).pipe(
      tap(() => this.tokenService.removeToken())
    );
  }
}