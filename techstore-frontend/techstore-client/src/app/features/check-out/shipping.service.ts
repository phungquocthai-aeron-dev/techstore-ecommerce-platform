import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';

import {
  ProvinceData,
  DistrictData,
  WardData
} from './models/shipping.model';

@Injectable({
  providedIn: 'root'
})
export class ShippingService {
  // ONLY GHN
  private baseUrl = environment.orderUrl + '/shipping';

  constructor(private http: HttpClient) {}

  // ===============================
  // GET PROVINCES
  // ===============================

  getProvinces(
    type: string
  ): Observable<ApiResponse<ProvinceData[]>> {

    return this.http.get<ApiResponse<ProvinceData[]>>(
      `${this.baseUrl}/${type}/provinces`
    );
  }

  // ===============================
  // GET DISTRICTS
  // ===============================

  getDistricts(
    type: string,
    provinceId: number
  ): Observable<ApiResponse<DistrictData[]>> {

    const params = new HttpParams().set('provinceId', provinceId);

    return this.http.get<ApiResponse<DistrictData[]>>(
      `${this.baseUrl}/${type}/districts`,
      { params }
    );
  }

  // ===============================
  // GET WARDS
  // ===============================

  getWards(
    type: string,
    districtId: number
  ): Observable<ApiResponse<WardData[]>> {

    const params = new HttpParams().set('districtId', districtId);

    return this.http.get<ApiResponse<WardData[]>>(
      `${this.baseUrl}/${type}/wards`,
      { params }
    );
  }

  // ===============================
  // CALCULATE SHIPPING FEE
  // ===============================

  calculateFee(
    type: string,
    addressId: number,
    weight: number
  ): Observable<ApiResponse<number>> {

    const params = new HttpParams()
      .set('addressId', addressId)
      .set('weight', weight);

    return this.http.get<ApiResponse<number>>(
      `${this.baseUrl}/${type}/fee`,
      { params }
    );
  }

}