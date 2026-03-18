export interface TransactionDetailRequest {
  variantId: number;
  quantity: number;
  cost: number;
  batchCode?: string;
}

export interface WarehouseTransactionCreateRequest {
  note?: string;
  transactionType: string;
  referenceType: string;
  orderId?: number;
  staffId: number;
  supplierId?: number;
  warehouseId: number;
  details: TransactionDetailRequest[];
}

export interface OrderItemRequest {
  variantId: number;
  quantity: number;
}

export interface InventoryExportRequest {
  items: OrderItemRequest[];
  orderId: number;
}