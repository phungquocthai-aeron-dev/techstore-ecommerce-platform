import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';

import {
  OrderResponse,
  OrderDetailResponse,
  CustomerOrderResponse,
  AdminOrderResponse
} from './models/order.model';

import {
  OrderCreateRequest
} from './models/order-request.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private baseUrl = environment.orderUrl + '/orders';

  constructor(private http: HttpClient) {}

  getAllOrders(
    status?: string
  ): Observable<ApiResponse<AdminOrderResponse[]>> {
 
    let params = new HttpParams();
 
    if (status) {
      params = params.set('status', status);
    }
 
    return this.http.get<ApiResponse<AdminOrderResponse[]>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // ===============================
  // GET ORDERS BY CUSTOMER
  // ===============================
  
  getOrdersByCustomer(
    customerId: number,
    status?: string
  ): Observable<ApiResponse<CustomerOrderResponse[]>> {
  
    let params = new HttpParams();
  
    if (status) {
      params = params.set('status', status);
    }
  
    return this.http.get<ApiResponse<CustomerOrderResponse[]>>(
      `${this.baseUrl}/customer/${customerId}`,
      { params }
    );
  }

  // ===============================
  // CREATE ORDER
  // ===============================

  createOrder(
    req: OrderCreateRequest
  ): Observable<ApiResponse<OrderResponse>> {

    return this.http.post<ApiResponse<OrderResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // ===============================
  // GET ORDER DETAIL
  // ===============================

  getOrderDetail(
    id: number
  ): Observable<ApiResponse<OrderDetailResponse>> {

    return this.http.get<ApiResponse<OrderDetailResponse>>(
      `${this.baseUrl}/order-detail/${id}`
    );
  }

  // ===============================
  // CONFIRM ORDER
  // ===============================

  confirmOrder(
    id: number,
    staffId: number
  ): Observable<ApiResponse<void>> {

    const params = new HttpParams().set('staffId', staffId);

    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/${id}/confirm`,
      {},
      { params }
    );
  }

  // ===============================
  // CANCEL ORDER
  // ===============================

  cancelOrder(
    id: number,
    staffId: number
  ): Observable<ApiResponse<void>> {

    const params = new HttpParams().set('staffId', staffId);

    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/${id}/cancel`,
      {},
      { params }
    );
  }

  // ===============================
  // UPDATE STATUS
  // ===============================

  updateStatus(
    id: number,
    status: string
  ): Observable<ApiResponse<void>> {

    const params = new HttpParams().set('status', status);

    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/${id}/status`,
      {},
      { params }
    );
  }

  // ===============================
  // PRINT SHIPPING LABEL
  // ===============================

  printLabel(
    id: number
  ): Observable<ApiResponse<string>> {

    return this.http.get<ApiResponse<string>>(
      `${this.baseUrl}/${id}/print-label`
    );
  }

}