// category.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { PageResponse } from '../../shared/models/page-response.model';
import {
  CategoryResponse,
  CategoryCreateRequest,
  CategoryUpdateRequest
} from './models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private baseUrl = environment.productUrl + '/categories';

  constructor(private http: HttpClient) {}

  // POST /categories
  create(req: CategoryCreateRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.post<ApiResponse<CategoryResponse>>(this.baseUrl, req);
  }

  // PUT /categories/{id}
  update(id: number, req: CategoryUpdateRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.put<ApiResponse<CategoryResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // DELETE /categories/{id}
  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  // GET /categories/{id}
  getById(id: number): Observable<ApiResponse<CategoryResponse>> {
    return this.http.get<ApiResponse<CategoryResponse>>(`${this.baseUrl}/${id}`);
  }

  // GET /categories?page=&size=&sortBy=&sortDirection=
  getAll(params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
  }): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    const httpParams = this.buildPageParams(params);
    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(
      this.baseUrl,
      { params: httpParams }
    );
  }

  // GET /categories/type/{categoryType}?page=&size=&sortBy=&sortDirection=
  getByType(
    categoryType: string,
    params?: {
      page?: number;
      size?: number;
      sortBy?: string;
      sortDirection?: string;
    }
  ): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    const httpParams = this.buildPageParams(params);
    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(
      `${this.baseUrl}/type/${categoryType}`,
      { params: httpParams }
    );
  }

  // GET /categories/search?keyword=&categoryType=&page=&size=&sortBy=&sortDirection=
  search(params: {
    keyword?: string;
    categoryType?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
  }): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    let httpParams = this.buildPageParams(params);
    if (params.keyword)      httpParams = httpParams.set('keyword', params.keyword);
    if (params.categoryType) httpParams = httpParams.set('categoryType', params.categoryType);

    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // Helper: build pagination params
  private buildPageParams(params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
  }): HttpParams {
    let httpParams = new HttpParams();
    if (params?.page != null)          httpParams = httpParams.set('page', params.page);
    if (params?.size != null)          httpParams = httpParams.set('size', params.size);
    if (params?.sortBy)                httpParams = httpParams.set('sortBy', params.sortBy);
    if (params?.sortDirection)         httpParams = httpParams.set('sortDirection', params.sortDirection);
    return httpParams;
  }
}