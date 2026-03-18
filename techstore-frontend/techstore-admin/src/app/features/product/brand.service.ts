import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';
import { PageResponse } from '../../shared/models/page-response.model';

import {
  BrandResponse
} from './models/brand.model';

import {
  BrandCreateRequest,
  BrandUpdateRequest
} from './models/brand-request.model';

@Injectable({
  providedIn: 'root'
})
export class BrandService {

  private baseUrl = environment.productUrl + '/brands';

  constructor(private http: HttpClient) {}

  // ===============================
  // CREATE
  // ===============================

  createBrand(
    req: BrandCreateRequest
  ): Observable<ApiResponse<BrandResponse>> {

    return this.http.post<ApiResponse<BrandResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // ===============================
  // UPDATE
  // ===============================

  updateBrand(
    id: number,
    req: BrandUpdateRequest
  ): Observable<ApiResponse<BrandResponse>> {

    return this.http.put<ApiResponse<BrandResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // ===============================
  // UPDATE STATUS
  // ===============================

  updateStatus(
    id: number,
    status: string
  ): Observable<ApiResponse<BrandResponse>> {

    return this.http.patch<ApiResponse<BrandResponse>>(
      `${this.baseUrl}/${id}/status`,
      { status }
    );
  }

  // ===============================
  // DELETE
  // ===============================

  deleteBrand(
    id: number
  ): Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET BY ID
  // ===============================

  getBrandById(
    id: number
  ): Observable<ApiResponse<BrandResponse>> {

    return this.http.get<ApiResponse<BrandResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET ALL
  // ===============================

  getAllBrands(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDirection: string = 'DESC'
  ): Observable<ApiResponse<PageResponse<BrandResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<ApiResponse<PageResponse<BrandResponse>>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // ===============================
  // GET BY STATUS
  // ===============================

  getBrandsByStatus(
    status: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDirection: string = 'DESC'
  ): Observable<ApiResponse<PageResponse<BrandResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<ApiResponse<PageResponse<BrandResponse>>>(
      `${this.baseUrl}/status/${status}`,
      { params }
    );
  }

  // ===============================
  // SEARCH
  // ===============================

  searchBrands(
    keyword?: string,
    status?: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDirection: string = 'DESC'
  ): Observable<ApiResponse<PageResponse<BrandResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<ApiResponse<PageResponse<BrandResponse>>>(
      `${this.baseUrl}/search`,
      { params }
    );
  }

}