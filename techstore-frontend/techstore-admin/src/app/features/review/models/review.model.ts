export interface CustomerResponse {
  id: number;
  fullName: string;
  email: string;
  avatar?: string;
}

export interface StaffResponse {
  id: number;
  fullName: string;
  email: string;
  avatar?: string;
}

export interface ReplyResponse {
  id: number;
  content: string;
  createdAt: string;
  status: string;
  staffId: number;
  staff?: StaffResponse;
}

export interface ReviewResponse {
  id: number;
  content: string;
  rating: number;
  createdAt: string;
  status: string;
  productId: number;
  variantId: number;
  orderDetailId: number;
  customerId: number;
  customer?: CustomerResponse;
  reviewed?: boolean;
  reply?: ReplyResponse;
}