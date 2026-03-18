import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { WarehouseResponse } from './models/warehouse.model';
import { WarehouseCreateRequest, WarehouseUpdateRequest } from './models/warehouse-request.model';

@Injectable({ providedIn: 'root' })
export class WarehouseService {
  private baseUrl = environment.warehouseUrl + '/warehouses';

  constructor(private http: HttpClient) {}

  create(req: WarehouseCreateRequest):
    Observable<ApiResponse<WarehouseResponse>> {
    return this.http.post<ApiResponse<WarehouseResponse>>(this.baseUrl, req);
  }

  update(id: number, req: WarehouseUpdateRequest):
    Observable<ApiResponse<WarehouseResponse>> {
    return this.http.put<ApiResponse<WarehouseResponse>>(`${this.baseUrl}/${id}`, req);
  }

  getById(id: number):
    Observable<ApiResponse<WarehouseResponse>> {
    return this.http.get<ApiResponse<WarehouseResponse>>(`${this.baseUrl}/${id}`);
  }

  getByName(name: string):
    Observable<ApiResponse<WarehouseResponse>> {
    return this.http.get<ApiResponse<WarehouseResponse>>(`${this.baseUrl}/name/${name}`);
  }

  getAll():
    Observable<ApiResponse<WarehouseResponse[]>> {
    return this.http.get<ApiResponse<WarehouseResponse[]>>(this.baseUrl);
  }

  getByStatus(status: string):
    Observable<ApiResponse<WarehouseResponse[]>> {
    return this.http.get<ApiResponse<WarehouseResponse[]>>(`${this.baseUrl}/status/${status}`);
  }

  getByAddress(addressId: string):
    Observable<ApiResponse<WarehouseResponse[]>> {
    return this.http.get<ApiResponse<WarehouseResponse[]>>(`${this.baseUrl}/address/${addressId}`);
  }

  delete(id: number):
    Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, status: string):
    Observable<ApiResponse<WarehouseResponse>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ApiResponse<WarehouseResponse>>(`${this.baseUrl}/${id}/status`, null, { params });
  }
}