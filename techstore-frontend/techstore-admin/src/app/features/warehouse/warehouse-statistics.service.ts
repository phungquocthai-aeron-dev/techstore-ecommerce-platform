import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { InboundCostStatResponse, PeriodType } from './models/warehouse-statistics.model';


@Injectable({ providedIn: 'root' })
export class WarehouseStatisticsService {
  private baseUrl = environment.warehouseUrl + '/statistics';

  constructor(private http: HttpClient) {}

  /**
   * Thống kê chi phí nhập kho theo ngày hôm nay (group theo giờ)
   * Không cần truyền from/to — backend tự tính 00:00 → now
   */
  getInboundCostToday(): Observable<ApiResponse<InboundCostStatResponse>> {
    const params = new HttpParams().set('periodType', 'TODAY');
    return this.http.get<ApiResponse<InboundCostStatResponse>>(
      `${this.baseUrl}/inbound-cost`,
      { params }
    );
  }

  /**
   * Thống kê chi phí nhập kho theo tháng
   * @param from  ISO string, e.g. '2024-01-01T00:00:00' — optional
   * @param to    ISO string — optional, default: now
   */
  getInboundCostMonthly(
    from?: string,
    to?: string
  ): Observable<ApiResponse<InboundCostStatResponse>> {
    let params = new HttpParams().set('periodType', 'MONTHLY');
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<ApiResponse<InboundCostStatResponse>>(
      `${this.baseUrl}/inbound-cost`,
      { params }
    );
  }

  /**
   * Thống kê chi phí nhập kho theo quý
   * @param from  optional
   * @param to    optional, default: now
   */
  getInboundCostQuarterly(
    from?: string,
    to?: string
  ): Observable<ApiResponse<InboundCostStatResponse>> {
    let params = new HttpParams().set('periodType', 'QUARTERLY');
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<ApiResponse<InboundCostStatResponse>>(
      `${this.baseUrl}/inbound-cost`,
      { params }
    );
  }

  /**
   * Thống kê chi phí nhập kho theo năm
   * @param from  optional
   * @param to    optional, default: now
   */
  getInboundCostYearly(
    from?: string,
    to?: string
  ): Observable<ApiResponse<InboundCostStatResponse>> {
    let params = new HttpParams().set('periodType', 'YEARLY');
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<ApiResponse<InboundCostStatResponse>>(
      `${this.baseUrl}/inbound-cost`,
      { params }
    );
  }

  /**
   * Thống kê chi phí nhập kho theo khoảng thời gian tùy chọn
   * @param from  bắt buộc khi dùng CUSTOM
   * @param to    bắt buộc khi dùng CUSTOM
   */
  getInboundCostCustom(
    from: string,
    to: string
  ): Observable<ApiResponse<InboundCostStatResponse>> {
    const params = new HttpParams()
      .set('periodType', 'CUSTOM')
      .set('from', from)
      .set('to', to);
    return this.http.get<ApiResponse<InboundCostStatResponse>>(
      `${this.baseUrl}/inbound-cost`,
      { params }
    );
  }

  /**
   * Gọi chung — dùng khi component tự quản lý periodType (dropdown, tab)
   * @param periodType  loại kỳ thống kê
   * @param from        optional
   * @param to          optional
   */
  getInboundCost(
    periodType: PeriodType,
    from?: string,
    to?: string
  ): Observable<ApiResponse<InboundCostStatResponse>> {
    let params = new HttpParams().set('periodType', periodType);
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<ApiResponse<InboundCostStatResponse>>(
      `${this.baseUrl}/inbound-cost`,
      { params }
    );
  }
}