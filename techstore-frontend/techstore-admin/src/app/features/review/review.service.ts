import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { PageResponse } from '../../shared/models/page-response.model';
import { ReviewResponse, ReplyResponse } from './models/review.model';
import {
  CreateReviewRequest,
  UpdateReviewRequest,
  CreateReplyRequest,
  UpdateReplyRequest,
  ReviewSearchRequest,
  ReplySearchRequest,
} from './models/review-request.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private baseUrl = environment.reviewUrl + '/reviews';

  constructor(private http: HttpClient) {}

  // ==================== REVIEW ====================

  createReview(req: CreateReviewRequest): Observable<ApiResponse<ReviewResponse>> {
    return this.http.post<ApiResponse<ReviewResponse>>(this.baseUrl, req);
  }

  updateReview(id: number, req: UpdateReviewRequest): Observable<ApiResponse<ReviewResponse>> {
    return this.http.put<ApiResponse<ReviewResponse>>(`${this.baseUrl}/${id}`, req);
  }

  deleteReview(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  /** GET /reviews — phân trang đơn giản theo productId + rating */
  getReviews(
    productId: number,
    rating?: number,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<ReviewResponse>>> {
    let params = new HttpParams()
      .set('productId', productId)
      .set('page', page)
      .set('size', size);

    if (rating != null) params = params.set('rating', rating);

    return this.http.get<ApiResponse<PageResponse<ReviewResponse>>>(this.baseUrl, { params });
  }

  /** GET /reviews/search — tìm kiếm nâng cao với nhiều filter */
  searchReviews(req: ReviewSearchRequest): Observable<ApiResponse<PageResponse<ReviewResponse>>> {
    const params = this.buildReviewSearchParams(req);
    return this.http.get<ApiResponse<PageResponse<ReviewResponse>>>(`${this.baseUrl}/search`, { params });
  }

  // ==================== REPLY ====================

  createReply(reviewId: number, req: CreateReplyRequest): Observable<ApiResponse<ReplyResponse>> {
    return this.http.post<ApiResponse<ReplyResponse>>(`${this.baseUrl}/${reviewId}/reply`, req);
  }

  updateReply(id: number, req: UpdateReplyRequest): Observable<ApiResponse<ReplyResponse>> {
    return this.http.put<ApiResponse<ReplyResponse>>(`${this.baseUrl}/reply/${id}`, req);
  }

  deleteReply(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/reply/${id}`);
  }

  /** GET /reviews/reply/search — tìm kiếm reply với filter */
  searchReplies(req: ReplySearchRequest): Observable<ApiResponse<PageResponse<ReplyResponse>>> {
    const params = this.buildReplySearchParams(req);
    return this.http.get<ApiResponse<PageResponse<ReplyResponse>>>(`${this.baseUrl}/reply/search`, { params });
  }

  // ==================== HELPERS ====================

  private buildReviewSearchParams(req: ReviewSearchRequest): HttpParams {
    let params = new HttpParams();

    if (req.productId != null)  params = params.set('productId',  req.productId);
    if (req.customerId != null) params = params.set('customerId', req.customerId);
    if (req.rating != null)     params = params.set('rating',     req.rating);
    if (req.status)             params = params.set('status',     req.status);
    if (req.hasReply != null)   params = params.set('hasReply',   req.hasReply);
    if (req.keyword)            params = params.set('keyword',    req.keyword);
    if (req.sortBy)             params = params.set('sortBy',     req.sortBy);
    if (req.sortDir)            params = params.set('sortDir',    req.sortDir);

    params = params
      .set('page', req.page ?? 0)
      .set('size', req.size ?? 10);

    return params;
  }

  private buildReplySearchParams(req: ReplySearchRequest): HttpParams {
    let params = new HttpParams();

    if (req.staffId != null) params = params.set('staffId', req.staffId);
    if (req.status)          params = params.set('status',  req.status);
    if (req.keyword)         params = params.set('keyword', req.keyword);
    if (req.sortDir)         params = params.set('sortDir', req.sortDir);

    params = params
      .set('page', req.page ?? 0)
      .set('size', req.size ?? 10);

    return params;
  }
}