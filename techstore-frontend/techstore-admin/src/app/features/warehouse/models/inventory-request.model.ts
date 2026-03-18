export interface InventoryUpdateRequest {
  stock: number;
  status?: string;
  batchCode?: string;
}