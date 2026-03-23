import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';

import {
  VariantResponse
} from './models/variant.model';

import {
  VariantCreateRequest,
  VariantUpdateRequest,
  VariantUpdateImageRequest
} from './models/variant-request.model';
import { PageResponse } from './models/page-response.model';

@Injectable({
  providedIn: 'root'
})
export class VariantService {

  private baseUrl = environment.productUrl + '/variants';
  private productVariantUrl = environment.productUrl + '/products';

  constructor(private http: HttpClient) {}

  // ===============================
// SEARCH VARIANTS
// ===============================

search(
  keyword: string = '',
  page: number = 0,
  size: number = 10,
  sortBy: string = 'id',
  sortDirection: string = 'DESC'
) {
  let params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sortBy', sortBy)
    .set('sortDirection', sortDirection);

  if (keyword) {
    params = params.set('keyword', keyword);
  }

  return this.http.get<ApiResponse<PageResponse<VariantResponse>>>(
    `${this.baseUrl}/search`,
    { params }
  );
}

  // ===============================
// GET ALL ACTIVE VARIANTS (PAGINATION)
// ===============================

getAll(
  page: number = 0,
  size: number = 10,
  sortBy: string = 'id',
  sortDirection: string = 'DESC'
): Observable<ApiResponse<PageResponse<VariantResponse>>> {

  const params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sortBy', sortBy)
    .set('sortDirection', sortDirection);

  return this.http.get<ApiResponse<PageResponse<VariantResponse>>>(
    `${this.baseUrl}/all`,
    { params }
  );
}

  // ===============================
  // GET VARIANT BY ID
  // ===============================

  getById(
    variantId: number
  ): Observable<ApiResponse<VariantResponse>> {

    return this.http.get<ApiResponse<VariantResponse>>(
      `${this.baseUrl}/${variantId}`
    );
  }

  // ===============================
  // GET VARIANT DETAIL (WITH STOCK)
  // ===============================

  getDetailById(
    variantId: number
  ): Observable<ApiResponse<VariantResponse>> {

    return this.http.get<ApiResponse<VariantResponse>>(
      `${this.baseUrl}/detail/${variantId}`
    );
  }

  // ===============================
  // GET VARIANTS BY IDS
  // ===============================

  getByIds(
    ids: number[]
  ): Observable<ApiResponse<VariantResponse[]>> {

    let params = new HttpParams();

    ids.forEach(id => {
      params = params.append('ids', id);
    });

    return this.http.get<ApiResponse<VariantResponse[]>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // ===============================
  // GET VARIANTS WITH STOCK (BATCH)
  // ===============================

  getVariantsWithStock(
    variantIds: number[]
  ): Observable<ApiResponse<VariantResponse[]>> {

    return this.http.post<ApiResponse<VariantResponse[]>>(
      `${this.baseUrl}/variants/detail/batch`,
      variantIds
    );
  }

  // ===============================
  // UPDATE VARIANT
  // ===============================

  updateVariant(
    variantId: number,
    req: VariantUpdateRequest
  ): Observable<ApiResponse<VariantResponse>> {

    return this.http.put<ApiResponse<VariantResponse>>(
      `${this.baseUrl}/${variantId}`,
      req
    );
  }

  // ===============================
  // UPDATE VARIANT IMAGE
  // ===============================

  updateVariantImage(
    variantId: number,
    file: File,
    data?: VariantUpdateImageRequest
  ): Observable<ApiResponse<VariantResponse>> {

    const formData = new FormData();

    formData.append('file', file);

    if (data) {
      formData.append('data', JSON.stringify(data));
    }

    return this.http.put<ApiResponse<VariantResponse>>(
      `${this.baseUrl}/${variantId}/image`,
      formData
    );
  }

  // ===============================
  // DELETE VARIANT
  // ===============================

  deleteVariant(
    variantId: number
  ): Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${variantId}`
    );
  }

  // ===============================
  // CREATE VARIANT FOR PRODUCT
  // ===============================

  createVariant(
    productId: number,
    req: VariantCreateRequest
  ): Observable<ApiResponse<VariantResponse>> {

    return this.http.post<ApiResponse<VariantResponse>>(
      `${this.productVariantUrl}/${productId}/variants`,
      req
    );
  }

  // ===============================
  // GET VARIANTS BY PRODUCT
  // ===============================

  getByProduct(
    productId: number
  ): Observable<ApiResponse<VariantResponse[]>> {

    return this.http.get<ApiResponse<VariantResponse[]>>(
      `${this.productVariantUrl}/${productId}/variants`
    );
  }

}