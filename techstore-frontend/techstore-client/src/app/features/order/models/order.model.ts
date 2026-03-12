export interface OrderResponse {
  id: number;
  totalPrice: number;
  shippingFee: number;
  vat: number;
  status: string;
  paymentStatus: string;
  paymentUrl: string;
}

export interface OrderDetailResponse {
  id: number;
  totalPrice: number;
  shippingFee: number;
  vat: number;
  status: string;
  paymentStatus: string;

  customerId: number;
  addressId: number;

  items: OrderItemResponse[];
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}