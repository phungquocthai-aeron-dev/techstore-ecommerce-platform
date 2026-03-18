import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { InventoryResponse, VariantStockResponse } from './models/inventory.model';
import { InventoryUpdateRequest } from './models/inventory-request.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private baseUrl = environment.warehouseUrl + '/inventory';

  constructor(private http: HttpClient) {}

  findAvailableInventory(warehouseId: number, variantId: number, requiredQuantity: number):
    Observable<ApiResponse<InventoryResponse[]>> {
    const params = new HttpParams()
      .set('warehouseId', warehouseId)
      .set('variantId', variantId)
      .set('requiredQuantity', requiredQuantity);
    return this.http.get<ApiResponse<InventoryResponse[]>>(`${this.baseUrl}/available`, { params });
  }

  update(id: number, req: InventoryUpdateRequest):
    Observable<ApiResponse<InventoryResponse>> {
    return this.http.put<ApiResponse<InventoryResponse>>(`${this.baseUrl}/${id}`, req);
  }

  getById(id: number):
    Observable<ApiResponse<InventoryResponse>> {
    return this.http.get<ApiResponse<InventoryResponse>>(`${this.baseUrl}/${id}`);
  }

  getByWarehouse(warehouseId: number):
    Observable<ApiResponse<InventoryResponse[]>> {
    return this.http.get<ApiResponse<InventoryResponse[]>>(`${this.baseUrl}/warehouse/${warehouseId}`);
  }

  getByVariant(variantId: number):
    Observable<ApiResponse<InventoryResponse[]>> {
    return this.http.get<ApiResponse<InventoryResponse[]>>(`${this.baseUrl}/variant/${variantId}`);
  }

  getByWarehouseAndVariant(warehouseId: number, variantId: number):
    Observable<ApiResponse<InventoryResponse[]>> {
    const params = new HttpParams()
      .set('warehouseId', warehouseId)
      .set('variantId', variantId);
    return this.http.get<ApiResponse<InventoryResponse[]>>(`${this.baseUrl}/search`, { params });
  }

  getAll():
    Observable<ApiResponse<InventoryResponse[]>> {
    return this.http.get<ApiResponse<InventoryResponse[]>>(this.baseUrl);
  }

  getByStatus(status: string):
    Observable<ApiResponse<InventoryResponse[]>> {
    return this.http.get<ApiResponse<InventoryResponse[]>>(`${this.baseUrl}/status/${status}`);
  }

  getTotalStockByVariant(variantId: number):
    Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.baseUrl}/variant/${variantId}/total-stock`);
  }

  getTotalStockByVariants(variantIds: number[]):
    Observable<ApiResponse<VariantStockResponse[]>> {
    return this.http.post<ApiResponse<VariantStockResponse[]>>(`${this.baseUrl}/variant/total-stock/batch`, variantIds);
  }
}