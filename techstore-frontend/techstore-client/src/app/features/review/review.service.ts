import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';
import { PageResponse } from '../../shared/models/page-response.model';

import {
  ReviewResponse,
  ReplyResponse
} from './models/review.model';

import {
  CreateReviewRequest,
  UpdateReviewRequest,
  CreateReplyRequest,
  UpdateReplyRequest
} from './models/review-request.model';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private baseUrl = environment.apiUrl + '/review/reviews';

  constructor(private http: HttpClient) {}

  // ===============================
  // CREATE REVIEW
  // ===============================

  createReview(req: CreateReviewRequest):
    Observable<ApiResponse<ReviewResponse>> {

    return this.http.post<ApiResponse<ReviewResponse>>(
      `${this.baseUrl}`,
      req
    );
  }

  // ===============================
  // UPDATE REVIEW
  // ===============================

  updateReview(id: number, req: UpdateReviewRequest):
    Observable<ApiResponse<ReviewResponse>> {

    return this.http.put<ApiResponse<ReviewResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // ===============================
  // DELETE REVIEW
  // ===============================

  deleteReview(id: number):
    Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/${id}`
    );
  }

  // ===============================
  // GET REVIEWS
  // ===============================

  getReviews(
    productId: number,
    rating?: number,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PageResponse<ReviewResponse>>> {

    let params = new HttpParams()
      .set('productId', productId)
      .set('page', page)
      .set('size', size);

    if (rating) {
      params = params.set('rating', rating);
    }

    return this.http.get<ApiResponse<PageResponse<ReviewResponse>>>(
      `${this.baseUrl}`,
      { params }
    );
  }

  // ===============================
  // CREATE REPLY
  // ===============================

  replyReview(
    reviewId: number,
    req: CreateReplyRequest
  ): Observable<ApiResponse<ReplyResponse>> {

    return this.http.post<ApiResponse<ReplyResponse>>(
      `${this.baseUrl}/${reviewId}/reply`,
      req
    );
  }

  // ===============================
  // UPDATE REPLY
  // ===============================

  updateReply(
    replyId: number,
    req: UpdateReplyRequest
  ): Observable<ApiResponse<ReplyResponse>> {

    return this.http.put<ApiResponse<ReplyResponse>>(
      `${this.baseUrl}/reply/${replyId}`,
      req
    );
  }

  // ===============================
  // DELETE REPLY
  // ===============================

  deleteReply(replyId: number):
    Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/reply/${replyId}`
    );
  }
}