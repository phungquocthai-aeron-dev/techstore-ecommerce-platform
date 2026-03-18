export interface SupplierCreateRequest {
  name: string;
  phone: string;
}

export interface SupplierUpdateRequest {
  name?: string;
  phone?: string;
  status?: string;
}