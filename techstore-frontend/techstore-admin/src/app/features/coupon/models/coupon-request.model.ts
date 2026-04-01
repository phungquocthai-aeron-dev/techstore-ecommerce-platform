export interface CouponRequest {
  name: string;
  discountType: string;
  discountValue: number;
  minOrderValue: number;
  maxDiscount: number;
  startDate: string;
  endDate: string;
  usageLimit: number;
  couponType: string;
}