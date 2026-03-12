import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
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

  constructor(private http: HttpClient) {}

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

  // GET /customers/search?id=&email=&phone=&fullName=
  search(params: {
    id?: number;
    email?: string;
    phone?: string;
    fullName?: string;
  }): Observable<ApiResponse<CustomerResponse[]>> {
    let httpParams = new HttpParams();
    if (params.id != null)       httpParams = httpParams.set('id', params.id);
    if (params.email)            httpParams = httpParams.set('email', params.email);
    if (params.phone)            httpParams = httpParams.set('phone', params.phone);
    if (params.fullName)         httpParams = httpParams.set('fullName', params.fullName);

    return this.http.get<ApiResponse<CustomerResponse[]>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // PATCH /customers/{id}
  updateInfo(id: number, req: CustomerUpdateRequest): Observable<ApiResponse<CustomerResponse>> {
    return this.http.patch<ApiResponse<CustomerResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // POST /customers/{id}/avatar (multipart)
  uploadAvatar(id: number, file: File): Observable<ApiResponse<void>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/${id}/avatar`,
      formData
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