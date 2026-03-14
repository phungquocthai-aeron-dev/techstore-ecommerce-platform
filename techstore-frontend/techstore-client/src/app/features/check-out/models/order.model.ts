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
  reviewed: boolean;

  items: OrderItemResponse[];
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}

export interface CustomerOrderResponse {
  orderId: number;

  totalPrice: number;
  shippingFee: number;
  vat: number;

  status: string;

  shippingCode: string;

  createdAt: string;
  expectedDeliveryTime: string;

  shippingProviderName: string;

  couponName?: string;

  address: string;

  items: CustomerOrderItemResponse[];
}

export interface CustomerOrderItemResponse {
  orderDetailId: number;
  variantId: number;

  name: string;
  image: string;

  quantity: number;
  price: number;
  reviewed: boolean;
}