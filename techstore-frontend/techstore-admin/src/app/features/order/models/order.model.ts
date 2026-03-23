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

export interface AdminOrderResponse {
  orderId:              number;
  customerId:           number;
  customerName:         string;
  customerEmail:        string;
  customerPhone:        string;
  totalPrice:           number;
  shippingFee:          number;
  vat:                  number;
  status:               string;
  shippingCode:         string;
  createdAt:            string;
  expectedDeliveryTime: string;
  shippingProviderName: string;
  couponName?:          string;
  address:              string;
  items:                CustomerOrderItemResponse[];
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

export interface RevenueDataPoint {
  label: string;
  revenue: number;
  orderCount: number;
}

export interface RevenueStatsResponse {
  totalRevenue: number;
  totalOrders: number;
  dataPoints: RevenueDataPoint[];
}

export interface TopVariantResponse {
  variantId: number;
  name: string;
  imageUrl: string | null;
  totalQuantitySold: number;
  totalRevenue: number;
}

export interface StatusCount {
  status: string;
  count: number;
  revenue: number;
}

export interface OrderSummaryResponse {
  totalOrders: number;
  totalRevenue: number;
  statusBreakdown: StatusCount[];
}

export interface TopLoyalCustomerResponse {
  customerId: number;
  fullName: string | null;
  email: string | null;
  phone: string | null;
  avatarUrl: string | null;
  orderCount: number;
  totalSpent: number;
  loyaltyScore: number;
}

// ===============================
// PRODUCT SALES
// ===============================

export interface SalesDataPoint {
  label: string;
  quantitySold: number;
}

export interface VariantSales {
  variantId: number;
  variantName: string;
  totalQuantitySold: number;
  dataPoints: SalesDataPoint[];
}

export interface ProductSalesResponse {
  productId: number;
  period: string;
  totalQuantitySold: number;
  variants: VariantSales[];
}