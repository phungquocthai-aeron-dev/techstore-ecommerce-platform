import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { WarehouseTransactionResponse } from './models/warehouse-transaction.model';
import {
  WarehouseTransactionCreateRequest,
  InventoryExportRequest
} from './models/warehouse-transaction-request.model';

@Injectable({ providedIn: 'root' })
export class WarehouseTransactionService {
  private baseUrl = environment.warehouseUrl + '/transactions';

  constructor(private http: HttpClient) {}

  createInbound(req: WarehouseTransactionCreateRequest):
    Observable<ApiResponse<WarehouseTransactionResponse>> {
    return this.http.post<ApiResponse<WarehouseTransactionResponse>>(`${this.baseUrl}/inbound`, req);
  }

  createOutbound(req: WarehouseTransactionCreateRequest):
    Observable<ApiResponse<WarehouseTransactionResponse>> {
    return this.http.post<ApiResponse<WarehouseTransactionResponse>>(`${this.baseUrl}/outbound`, req);
  }

  exportInventory(req: InventoryExportRequest):
    Observable<ApiResponse<number[]>> {
    return this.http.post<ApiResponse<number[]>>(`${this.baseUrl}/export`, req);
  }

  getById(id: number):
    Observable<ApiResponse<WarehouseTransactionResponse>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse>>(`${this.baseUrl}/${id}`);
  }

  getAll():
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(this.baseUrl);
  }

  getByWarehouse(warehouseId: number):
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(`${this.baseUrl}/warehouse/${warehouseId}`);
  }

  getBySupplier(supplierId: number):
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(`${this.baseUrl}/supplier/${supplierId}`);
  }

  getByType(type: string):
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(`${this.baseUrl}/type/${type}`);
  }

  getByStatus(status: string):
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(`${this.baseUrl}/status/${status}`);
  }

  getByOrderId(orderId: number):
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(`${this.baseUrl}/order/${orderId}`);
  }

  getByDateRange(startDate: string, endDate: string):
    Observable<ApiResponse<WarehouseTransactionResponse[]>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<ApiResponse<WarehouseTransactionResponse[]>>(`${this.baseUrl}/date-range`, { params });
  }

  cancel(id: number):
    Observable<ApiResponse<WarehouseTransactionResponse>> {
    return this.http.put<ApiResponse<WarehouseTransactionResponse>>(`${this.baseUrl}/${id}/cancel`, null);
  }

  updateStatus(id: number, status: string):
    Observable<ApiResponse<WarehouseTransactionResponse>> {
    return this.http.put<ApiResponse<WarehouseTransactionResponse>>(`${this.baseUrl}/${id}/status`, status);
  }
}