export interface PaymentMethodCreateRequest {
  name: string;
  status: string;
}

export interface PaymentMethodUpdateRequest {
  name?: string;
  status?: string;
}

export interface PaymentMethodStatusUpdateRequest {
  status: string;
}

export interface PaymentMethodSearchRequest {
  keyword?: string;
  status?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}