import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { SupplierResponse } from './models/supplier.model';
import { SupplierCreateRequest, SupplierUpdateRequest } from './models/supplier-request.model';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  private baseUrl = environment.warehouseUrl + '/suppliers';

  constructor(private http: HttpClient) {}

  create(req: SupplierCreateRequest):
    Observable<ApiResponse<SupplierResponse>> {
    return this.http.post<ApiResponse<SupplierResponse>>(this.baseUrl, req);
  }

  update(id: number, req: SupplierUpdateRequest):
    Observable<ApiResponse<SupplierResponse>> {
    return this.http.put<ApiResponse<SupplierResponse>>(`${this.baseUrl}/${id}`, req);
  }

  getById(id: number):
    Observable<ApiResponse<SupplierResponse>> {
    return this.http.get<ApiResponse<SupplierResponse>>(`${this.baseUrl}/${id}`);
  }

  getByPhone(phone: string):
    Observable<ApiResponse<SupplierResponse>> {
    return this.http.get<ApiResponse<SupplierResponse>>(`${this.baseUrl}/phone/${phone}`);
  }

  getAll():
    Observable<ApiResponse<SupplierResponse[]>> {
    return this.http.get<ApiResponse<SupplierResponse[]>>(this.baseUrl);
  }

  getByStatus(status: string):
    Observable<ApiResponse<SupplierResponse[]>> {
    return this.http.get<ApiResponse<SupplierResponse[]>>(`${this.baseUrl}/status/${status}`);
  }

  searchByName(name: string):
    Observable<ApiResponse<SupplierResponse[]>> {
    const params = new HttpParams().set('name', name);
    return this.http.get<ApiResponse<SupplierResponse[]>>(`${this.baseUrl}/search`, { params });
  }

  delete(id: number):
    Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string):
    Observable<ApiResponse<SupplierResponse>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ApiResponse<SupplierResponse>>(`${this.baseUrl}/${id}/status`, null, { params });
  }
}