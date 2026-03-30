import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { CouponConfigResponseDTO, RedeemCouponRequestDTO, RedeemCouponResponseDTO, UserCoupon } from './models/game.model';

@Injectable({ providedIn: 'root' })
export class CouponService {
  private baseUrl = `${environment.quizGameUrl}/coupons`;

  private availableCouponsSubject = new BehaviorSubject<CouponConfigResponseDTO[]>([]);
  availableCoupons$ = this.availableCouponsSubject.asObservable();

  private couponHistorySubject = new BehaviorSubject<UserCoupon[]>([]);
  couponHistory$ = this.couponHistorySubject.asObservable();

  constructor(private http: HttpClient) {}

  // ─── Available coupons ─────────────────────────────────────────────
  loadAvailableCoupons(userId: number): Observable<ApiResponse<CouponConfigResponseDTO[]>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<ApiResponse<CouponConfigResponseDTO[]>>(
      `${this.baseUrl}/available`,
      { params }
    ).pipe(
      tap(res => this.availableCouponsSubject.next(res.result ?? []))
    );
  }

  // ─── Redeem coupon ─────────────────────────────────────────────────
  redeemCoupon(userId: number, couponConfigId: number): Observable<ApiResponse<RedeemCouponResponseDTO>> {
    const body: RedeemCouponRequestDTO = { userId, couponConfigId };
    return this.http.post<ApiResponse<RedeemCouponResponseDTO>>(
      `${this.baseUrl}/redeem`,
      body
    );
  }

  // ─── Coupon history ────────────────────────────────────────────────
  loadCouponHistory(userId: number): Observable<ApiResponse<UserCoupon[]>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<ApiResponse<UserCoupon[]>>(
      `${this.baseUrl}/history`,
      { params }
    ).pipe(
      tap(res => this.couponHistorySubject.next(res.result ?? []))
    );
  }

  get availableCoupons(): CouponConfigResponseDTO[] {
    return this.availableCouponsSubject.getValue();
  }

  get couponHistory(): UserCoupon[] {
    return this.couponHistorySubject.getValue();
  }
}