export interface AddItemRequest {
  variantId: number;
  quantity: number;
}

export interface UpdateQuantityRequest {
  quantity: number;
}

export interface CheckoutRequest {
  cartDetailIds: number[];
}