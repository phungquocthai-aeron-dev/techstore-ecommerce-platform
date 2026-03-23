import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';

import {
  CouponRequest
} from './models/coupon-request.model';

import {
  CouponResponse
} from './models/coupon.model';

@Injectable({
  providedIn: 'root'
})
export class CouponService {

  private baseUrl = environment.orderUrl + '/coupons';

  constructor(private http: HttpClient) {}

  // ===============================
  // CREATE
  // ===============================

  createCoupon(
    req: CouponRequest
  ): Observable<ApiResponse<CouponResponse>> {

    return this.http.post<ApiResponse<CouponResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // ===============================
  // GET BY ID
  // ===============================

  getById(
    id: number
  ): Observable<ApiResponse<CouponResponse>> {

    return this.http.get<ApiResponse<CouponResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET AVAILABLE COUPONS
  // ===============================
  
  getAvailableCoupons(): Observable<ApiResponse<CouponResponse[]>> {
  
    return this.http.get<ApiResponse<CouponResponse[]>>(
      `${this.baseUrl}/available`
    );
  }

  // ===============================
  // GET ALL
  // ===============================

  getAll(): Observable<ApiResponse<CouponResponse[]>> {

    return this.http.get<ApiResponse<CouponResponse[]>>(
      `${this.baseUrl}`
    );
  }

  // ===============================
  // UPDATE
  // ===============================

  updateCoupon(
    id: number,
    req: CouponRequest
  ): Observable<ApiResponse<CouponResponse>> {

    return this.http.put<ApiResponse<CouponResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // ===============================
  // DELETE
  // ===============================

  deleteCoupon(
    id: number
  ): Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${id}`
    );
  }

}