import { SupplierResponse } from '../../supplier/models/supplier.model';
import { WarehouseResponse } from './warehouse.model';
import { InventoryResponse } from './inventory.model';

export interface TransactionDetailResponse {
  id: number;
  quantity: number;
  cost: number;
  variantId: number;
  inventory: InventoryResponse;
}

export interface WarehouseTransactionResponse {
  id: number;
  note: string;
  transactionType: string;
  referenceType: string;
  orderId: string;
  staffId: number;
  status: string;
  createdAt: string;
  supplier: SupplierResponse;
  warehouse: WarehouseResponse;
  details: TransactionDetailResponse[];
}