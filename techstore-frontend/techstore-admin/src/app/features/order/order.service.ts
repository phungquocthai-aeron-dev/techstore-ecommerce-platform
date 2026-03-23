import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';

import {
  OrderResponse,
  OrderDetailResponse,
  CustomerOrderResponse,
  AdminOrderResponse,
  OrderSummaryResponse,
  TopVariantResponse,
  RevenueStatsResponse,
  TopLoyalCustomerResponse,
  ProductSalesResponse
} from './models/order.model';

import {
  OrderCreateRequest,
  OrderSummaryParams,
  ProductSalesParams,
  RevenueStatsParams,
  TopLoyalCustomersParams,
  TopVariantsParams
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

  // ===============================
  // REVENUE STATS
  // ===============================

  getRevenueStats(
    params?: RevenueStatsParams
  ): Observable<ApiResponse<RevenueStatsResponse>> {

    let httpParams = new HttpParams();

    if (params?.period) {
      httpParams = httpParams.set('period', params.period);
    }
    if (params?.from) {
      httpParams = httpParams.set('from', params.from);
    }
    if (params?.to) {
      httpParams = httpParams.set('to', params.to);
    }

    return this.http.get<ApiResponse<RevenueStatsResponse>>(
      `${this.baseUrl}/stats/revenue`,
      { params: httpParams }
    );
  }

  // ===============================
  // TOP VARIANTS
  // ===============================

  getTopVariants(
    params?: TopVariantsParams
  ): Observable<ApiResponse<TopVariantResponse[]>> {

    let httpParams = new HttpParams();

    if (params?.top != null) {
      httpParams = httpParams.set('top', params.top);
    }
    if (params?.from) {
      httpParams = httpParams.set('from', params.from);
    }
    if (params?.to) {
      httpParams = httpParams.set('to', params.to);
    }

    return this.http.get<ApiResponse<TopVariantResponse[]>>(
      `${this.baseUrl}/stats/top-variants`,
      { params: httpParams }
    );
  }

  // ===============================
  // ORDER SUMMARY
  // ===============================

  getOrderSummary(
    params?: OrderSummaryParams
  ): Observable<ApiResponse<OrderSummaryResponse>> {

    let httpParams = new HttpParams();

    if (params?.status && params.status !== 'ALL') {
      httpParams = httpParams.set('status', params.status);
    }
    if (params?.from) {
      httpParams = httpParams.set('from', params.from);
    }
    if (params?.to) {
      httpParams = httpParams.set('to', params.to);
    }

    return this.http.get<ApiResponse<OrderSummaryResponse>>(
      `${this.baseUrl}/stats/summary`,
      { params: httpParams }
    );
  }

// ===============================
// TOP LOYAL CUSTOMERS
// ===============================

  getTopLoyalCustomers(
  params?: TopLoyalCustomersParams
): Observable<ApiResponse<TopLoyalCustomerResponse[]>> {

  let httpParams = new HttpParams();

  if (params?.top != null) {
    httpParams = httpParams.set('top', params.top);
  }
  if (params?.period) {
    httpParams = httpParams.set('period', params.period);
  }
  if (params?.from) {
    httpParams = httpParams.set('from', params.from);
  }
  if (params?.to) {
    httpParams = httpParams.set('to', params.to);
  }

  return this.http.get<ApiResponse<TopLoyalCustomerResponse[]>>(
    `${this.baseUrl}/stats/top-loyal-customers`,
    { params: httpParams }
  );
}

// ===============================
// PRODUCT SALES
// ===============================

getProductSales(
  productId: number,
  params?: ProductSalesParams
): Observable<ApiResponse<ProductSalesResponse>> {

  let httpParams = new HttpParams();

  if (params?.period) {
    httpParams = httpParams.set('period', params.period);
  }
  if (params?.from) {
    httpParams = httpParams.set('from', params.from);
  }
  if (params?.to) {
    httpParams = httpParams.set('to', params.to);
  }

  return this.http.get<ApiResponse<ProductSalesResponse>>(
    `${this.baseUrl}/stats/product-sales/${productId}`,
    { params: httpParams }
  );
}
}