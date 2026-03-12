import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { AddressResponse, AddressRequest } from './models/address.model';

@Injectable({
  providedIn: 'root'
})
export class AddressService {

  private baseUrl = environment.orderUrl + '/addresses';

  constructor(private http: HttpClient) {}

  // POST /addresses?customerId=
  create(customerId: number, req: AddressRequest): Observable<ApiResponse<AddressResponse>> {
    const params = new HttpParams().set('customerId', customerId);
    return this.http.post<ApiResponse<AddressResponse>>(this.baseUrl, req, { params });
  }

  // GET /addresses/customer/{customerId}
  getByCustomerId(customerId: number): Observable<ApiResponse<AddressResponse[]>> {
    return this.http.get<ApiResponse<AddressResponse[]>>(
      `${this.baseUrl}/customer/${customerId}`
    );
  }

  // PUT /addresses/{id}
  update(id: number, req: AddressRequest): Observable<ApiResponse<AddressResponse>> {
    return this.http.put<ApiResponse<AddressResponse>>(
      `${this.baseUrl}/${id}`,
      req
    );
  }

  // DELETE /addresses/{id}
  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}