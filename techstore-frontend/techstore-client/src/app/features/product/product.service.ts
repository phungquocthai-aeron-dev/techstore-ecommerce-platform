import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import {
  ProductResponse,
  ProductListResponse
} from './models/product.model';

import {
  ProductCreateRequest,
  ProductUpdateRequest,
  ProductStatusUpdateRequest,
  ProductSearchRequest
} from './models/product-request.model';

import { ApiResponse } from '../../shared/models/api-response.model';
import { PageResponse } from '../../shared/models/page-response.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private baseUrl = environment.productUrl + '/products'; 
  // vì BE là /products

  constructor(private http: HttpClient) {}

  // ===============================
  // CREATE
  // ===============================

  createProduct(req: ProductCreateRequest):
    Observable<ApiResponse<ProductResponse>> {
    return this.http.post<ApiResponse<ProductResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // ===============================
  // UPDATE INFO
  // ===============================

  updateProduct(id: number, req: ProductUpdateRequest):
    Observable<ApiResponse<ProductResponse>> {
    return this.http.put<ApiResponse<ProductResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // ===============================
  // UPDATE STATUS
  // ===============================

  updateStatus(id: number, status: string):
    Observable<ApiResponse<ProductResponse>> {

    const body: ProductStatusUpdateRequest = { status };

    return this.http.patch<ApiResponse<ProductResponse>>(
      `${this.baseUrl}/${id}/status`,
      body
    );
  }

  // ===============================
  // UPDATE IMAGES (MULTIPART)
  // ===============================

  updateImages(
    id: number,
    files: File[],
    images?: any[]
  ): Observable<ApiResponse<ProductResponse>> {

    const formData = new FormData();

    files.forEach(file => {
      formData.append('files', file);
    });

    if (images) {
      formData.append('images', JSON.stringify(images));
    }

    return this.http.post<ApiResponse<ProductResponse>>(
      `${this.baseUrl}/${id}/images`,
      formData
    );
  }

  // ===============================
  // GET BY ID
  // ===============================

  getById(id: number):
    Observable<ApiResponse<ProductResponse>> {
    return this.http.get<ApiResponse<ProductResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET ALL (PAGING)
  // ===============================

  getAll(
    page = 0,
    size = 10,
    sortBy = 'id',
    sortDirection = 'DESC'
  ): Observable<ApiResponse<PageResponse<ProductListResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<ApiResponse<PageResponse<ProductListResponse>>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // ===============================
  // GET BY CATEGORY TYPE
  // ===============================

  getByCategoryType(
    categoryType: string,
    page = 0,
    size = 10,
    sortBy = 'id',
    sortDirection = 'DESC'
  ): Observable<ApiResponse<PageResponse<ProductListResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<ApiResponse<PageResponse<ProductListResponse>>>(
      `${this.baseUrl}/category/type/${categoryType}`,
      { params }
    );
  }

  // ===============================
  // GET BY CATEGORY ID
  // ===============================

  getByCategoryId(
  categoryId: number,
  page = 0,
  size = 10,
  sortBy = 'id',
  sortDirection = 'DESC',
  brandIds?: number[],
  minPrice?: number,
  maxPrice?: number
): Observable<ApiResponse<PageResponse<ProductListResponse>>> {

  let params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sortBy', sortBy)
    .set('sortDirection', sortDirection);

  if (brandIds && brandIds.length > 0) {
    brandIds.forEach(id => {
      params = params.append('brandIds', id);
    });
  }

  if (minPrice != null) {
    params = params.set('minPrice', minPrice);
  }

  if (maxPrice != null) {
    params = params.set('maxPrice', maxPrice);
  }

  return this.http.get<ApiResponse<PageResponse<ProductListResponse>>>(
    `${this.baseUrl}/category/${categoryId}`,
    { params }
  );
}

  // ===============================
  // GET LATEST
  // ===============================

  getLatest(limit = 10):
    Observable<ApiResponse<ProductListResponse[]>> {

    return this.http.get<ApiResponse<ProductListResponse[]>>(
      `${this.baseUrl}/latest?limit=${limit}`
    );
  }

  // ===============================
  // SEARCH
  // ===============================

  search(req: ProductSearchRequest):
    Observable<ApiResponse<PageResponse<ProductListResponse>>> {
    
    let params = new HttpParams();
    
    Object.entries(req).forEach(([key, value]) => {
    
      if (value === undefined || value === null) return;
    
      // nếu là array
      if (Array.isArray(value)) {
        value.forEach(v => {
          params = params.append(key, v);
        });
      }
      // nếu là value thường
      else {
        params = params.set(key, value);
      }
    
    });
  
    return this.http.get<ApiResponse<PageResponse<ProductListResponse>>>(
      `${this.baseUrl}/search`,
      { params }
    );
  }

  // ===============================
  // FIND BY VARIANT ID
  // ===============================

  findByVariantId(variantId: number):
    Observable<ApiResponse<ProductResponse>> {

    return this.http.get<ApiResponse<ProductResponse>>(
      `${this.baseUrl}/variant/${variantId}`
    );
  }
}