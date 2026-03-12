export interface OrderCreateRequest {
  customerId: number;
  addressId: number;
  paymentMethod: string;
  paymentMethodId: number;
  couponId?: number;
  shippingProviderId: number;

  items: OrderItemRequest[];
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}