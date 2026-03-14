import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';
import { PageResponse } from '../../shared/models/page-response.model';

import {
  PaymentMethodResponse
} from './models/payment-method.model';

import {
  PaymentMethodCreateRequest,
  PaymentMethodUpdateRequest,
  PaymentMethodStatusUpdateRequest,
  PaymentMethodSearchRequest
} from './models/payment-method-request.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentMethodService {

  private baseUrl = environment.orderUrl + '/payment-methods';

  constructor(private http: HttpClient) {}

  // ===============================
  // CREATE PAYMENT METHOD
  // ===============================

  createPaymentMethod(
    req: PaymentMethodCreateRequest
  ): Observable<ApiResponse<PaymentMethodResponse>> {

    return this.http.post<ApiResponse<PaymentMethodResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // ===============================
  // UPDATE PAYMENT METHOD
  // ===============================

  updatePaymentMethod(
    id: number,
    req: PaymentMethodUpdateRequest
  ): Observable<ApiResponse<PaymentMethodResponse>> {

    return this.http.put<ApiResponse<PaymentMethodResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // ===============================
  // UPDATE STATUS
  // ===============================

  updateStatus(
    id: number,
    req: PaymentMethodStatusUpdateRequest
  ): Observable<ApiResponse<PaymentMethodResponse>> {

    return this.http.patch<ApiResponse<PaymentMethodResponse>>(
      `${this.baseUrl}/${id}/status`,
      req
    );
  }

  // ===============================
  // DELETE
  // ===============================

  deletePaymentMethod(
    id: number
  ): Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET BY ID
  // ===============================

  getPaymentMethodById(
    id: number
  ): Observable<ApiResponse<PaymentMethodResponse>> {

    return this.http.get<ApiResponse<PaymentMethodResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET ALL (PAGING)
  // ===============================

  getAllPaymentMethods(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDirection: string = 'DESC'
  ): Observable<ApiResponse<PageResponse<PaymentMethodResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<ApiResponse<PageResponse<PaymentMethodResponse>>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // ===============================
  // SEARCH PAYMENT METHODS
  // ===============================

  searchPaymentMethods(
    req: PaymentMethodSearchRequest
  ): Observable<ApiResponse<PageResponse<PaymentMethodResponse>>> {

    let params = new HttpParams();

    if (req.keyword) params = params.set('keyword', req.keyword);
    if (req.status) params = params.set('status', req.status);

    params = params
      .set('page', req.page ?? 0)
      .set('size', req.size ?? 10)
      .set('sortBy', req.sortBy ?? 'id')
      .set('sortDirection', req.sortDirection ?? 'DESC');

    return this.http.get<ApiResponse<PageResponse<PaymentMethodResponse>>>(
      `${this.baseUrl}/search`,
      { params }
    );
  }

}