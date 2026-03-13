import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { ApiResponse } from '../../shared/models/api-response.model';

import {
  CartResponse
} from './models/cart.model';

import {
  AddItemRequest,
  UpdateQuantityRequest,
  CheckoutRequest
} from './models/cart-request.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private baseUrl = environment.cartUrl;

  constructor(private http: HttpClient) {}

  // ===============================
  // GET CART
  // ===============================

  getCart(): Observable<ApiResponse<CartResponse>> {
    return this.http.get<ApiResponse<CartResponse>>(
      `${this.baseUrl}/mycart`
    );
  }

  // ===============================
  // ADD ITEM
  // ===============================

  addItem(req: AddItemRequest):
    Observable<ApiResponse<void>> {

    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/items`,
      req
    );
  }

  // ===============================
  // UPDATE QUANTITY
  // ===============================

  updateQuantity(
    variantId: number,
    req: UpdateQuantityRequest
  ): Observable<ApiResponse<void>> {

    return this.http.put<ApiResponse<void>>(
      `${this.baseUrl}/items/${variantId}`,
      req
    );
  }

  // ===============================
  // REMOVE ITEM
  // ===============================

  removeItem(
    variantId: number
  ): Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/items/${variantId}`
    );
  }

  // ===============================
  // CHECKOUT
  // ===============================

  checkout(req: CheckoutRequest):
    Observable<ApiResponse<void>> {

    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/checkout`,
      req
    );
  }
}