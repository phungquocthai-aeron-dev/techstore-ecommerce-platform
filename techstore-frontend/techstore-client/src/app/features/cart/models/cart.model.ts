export interface CartItemResponse {
  id: number;
  variantId: number;
  quantity: number;
  price: number;
  subTotal: number;
}

export interface CartResponse {
  cartId: number;
  customerId: number;
  totalPrice: number;
  items: CartItemResponse[];
}