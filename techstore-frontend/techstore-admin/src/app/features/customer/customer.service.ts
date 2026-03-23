import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

import { ApiResponse } from '../../shared/models/api-response.model';
import { Customer } from './models/customer.model';
import { CustomerUpdateRequest } from './models/customer-update.model';
import { PageResponse } from './models/page-response.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private baseUrl = `${environment.userUrl}/customers`;

  constructor(private http: HttpClient) {}

  // Get by ID
  getById(id: number): Observable<ApiResponse<Customer>> {
    return this.http.get<ApiResponse<Customer>>(
      `${this.baseUrl}/${id}`
    );
  }

  // Get all
  getAll(): Observable<ApiResponse<Customer[]>> {
    return this.http.get<ApiResponse<Customer[]>>(
      `${this.baseUrl}/all`
    );
  }

getAllPaged(params: {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
} = {}): Observable<ApiResponse<PageResponse<Customer>>> {

  const httpParams = new HttpParams()
    .set('page',    params.page    ?? 0)
    .set('size',    params.size    ?? 10)
    .set('sortBy',  params.sortBy  ?? 'id')
    .set('sortDir', params.sortDir ?? 'asc');

  return this.http.get<ApiResponse<PageResponse<Customer>>>(
    `${this.baseUrl}/paged`,
    { params: httpParams }
  );
}

  // Search
  search(params: {
    id?: number;
    email?: string;
    phone?: string;
    fullName?: string;
  }): Observable<ApiResponse<Customer[]>> {

    let httpParams = new HttpParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        httpParams = httpParams.set(key, value);
      }
    });

    return this.http.get<ApiResponse<Customer[]>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // Update info
  updateInfo(id: number, request: CustomerUpdateRequest): Observable<ApiResponse<Customer>> {
    return this.http.patch<ApiResponse<Customer>>(
      `${this.baseUrl}/${id}`,
      request
    );
  }

  // Upload avatar
  uploadAvatar(id: number, file: File): Observable<ApiResponse<void>> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/${id}/avatar`,
      formData
    );
  }

  // Update password
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

  // Update status
  updateStatus(id: number, status: string): Observable<ApiResponse<void>> {

    const params = new HttpParams()
      .set('status', status);

    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/${id}/status`,
      null,
      { params }
    );
  }
}