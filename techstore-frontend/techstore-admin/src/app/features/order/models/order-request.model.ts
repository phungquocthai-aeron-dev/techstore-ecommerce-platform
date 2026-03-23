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
  variantId: number;
  quantity: number;
}

export type RevenuePeriod = 'TODAY' | 'MONTH' | 'QUARTER' | 'YEAR' | 'CUSTOM';

export interface RevenueStatsParams {
  period?: RevenuePeriod;
  from?: string;   // ISO date: "2024-03-01"
  to?: string;
}

export interface TopVariantsParams {
  top?: number;
  from?: string;
  to?: string;
}

export interface OrderSummaryParams {
  status?: string;
  from?: string;
  to?: string;
}

export type LoyaltyPeriod = 'TODAY' | 'MONTH' | 'QUARTER' | 'YEAR' | 'CUSTOM';

export interface TopLoyalCustomersParams {
  top?: number;
  period?: LoyaltyPeriod;
  from?: string;
  to?: string;
}

export type SalesPeriod = 'TODAY' | 'MONTH' | 'QUARTER' | 'YEAR' | 'CUSTOM';

export interface ProductSalesParams {
  period?: SalesPeriod;
  from?: string;
  to?: string;
}