export interface VariantInfo {
  id: number;
  productId: number;
  color: string;
  price: number;
  status: string;
  imageUrl: string;
}

export interface InventoryResponse {
  id: number;
  stock: number;
  updatedAt: string;
  status: string;
  variantId: number;
  batchCode: string;
  warehouseId: number;
  warehouseName: string;
  variantInfo: VariantInfo;
  cost: number;
}

export interface VariantStockResponse {
  variantId: number;
  stock: number;
}