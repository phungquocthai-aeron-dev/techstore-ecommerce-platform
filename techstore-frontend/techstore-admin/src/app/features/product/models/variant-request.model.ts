export interface VariantCreateRequest {
  color: string;
  price: number;
  status: string;
  weight: number;
}

export interface VariantUpdateRequest {
  color: string;
  price: number;
  status: string;
  weight: number;
}

export interface VariantUpdateImageRequest {
  color?: string;
  price?: number;
  status?: string;
  weight?: number;
}