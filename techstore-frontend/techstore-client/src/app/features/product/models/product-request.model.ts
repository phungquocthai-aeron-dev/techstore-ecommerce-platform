export interface ProductSpecRequest {
  specKey: string;
  specValue: string;
}

export interface ProductCreateRequest {
  name: string;
  description: string;
  performanceScore: number;
  powerConsumption: number;
  status: string;

  brandId: number;
  categoryId: number;

  specs: ProductSpecRequest[];
}

export interface ProductUpdateRequest extends ProductCreateRequest {}

export interface ProductStatusUpdateRequest {
  status: string;
}

export interface ProductSearchRequest {
  keyword?: string;
  brandName?: string;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}