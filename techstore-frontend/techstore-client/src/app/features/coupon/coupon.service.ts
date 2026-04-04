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
import { TokenService } from '../../core/services/token.service';

@Injectable({
  providedIn: 'root'
})
export class CouponService {

  private baseUrl = environment.orderUrl + '/coupons';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {}

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
  // GET ALL
  // ===============================

  getAll(): Observable<ApiResponse<CouponResponse[]>> {

    return this.http.get<ApiResponse<CouponResponse[]>>(
      `${this.baseUrl}`
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
  // GET PRIVATE COUPONS
  // ===============================
  getPrivateCoupons(): Observable<ApiResponse<CouponResponse[]>> {
    return this.http.get<ApiResponse<CouponResponse[]>>(
      `${this.baseUrl}/private`
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

  getMyCoupons(): Observable<ApiResponse<CouponResponse[]>> {
  const customerId = this.tokenService.getUserId();

  return this.http.get<ApiResponse<CouponResponse[]>>(
    `${this.baseUrl}/customer/${customerId}`
  );
}

assignCouponToCustomer(
  couponId: number
): Observable<ApiResponse<void>> {

  const customerId = this.tokenService.getUserId();

  return this.http.post<ApiResponse<void>>(
    `${this.baseUrl}/customer/${customerId}/${couponId}`,
    {}
  );
}

removeCouponFromCustomer(
  couponId: number
): Observable<ApiResponse<void>> {

  const customerId = this.tokenService.getUserId();

  return this.http.delete<ApiResponse<void>>(
    `${this.baseUrl}/customer/${customerId}/${couponId}`
  );
}
}