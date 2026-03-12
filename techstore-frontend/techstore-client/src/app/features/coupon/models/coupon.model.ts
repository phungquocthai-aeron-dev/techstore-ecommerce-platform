export interface CouponResponse {
  id: number;
  name: string;
  discountType: string;
  discountValue: number;
  minOrderValue: number;
  maxDiscount: number;
  startDate: string;
  endDate: string;
  usageLimit: number;
  usedCount: number;
  status: string;
  createdAt: string;
}