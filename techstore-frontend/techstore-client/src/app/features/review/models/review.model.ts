export interface CustomerResponse {
  id: number;
  fullName: string;
  avatarUrl: string;
}

export interface StaffResponse {
  id: number;
  fullName: string;
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
  reply?: ReplyResponse;
}