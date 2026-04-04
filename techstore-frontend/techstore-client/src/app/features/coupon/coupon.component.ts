import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CouponService } from '../coupon/coupon.service';
import { CouponResponse } from '../coupon/models/coupon.model';

@Component({
  selector: 'app-promotions',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './coupon.component.html',
  styleUrls: ['./coupon.component.css']
})
export class PromotionsComponent implements OnInit, OnDestroy {

  coupons: CouponResponse[] = [];
  loading = true;
  copiedId: number | null = null;

  private tickInterval: any;
  tickerItems = [
    '🎉 Ưu đãi độc quyền mỗi ngày',
    '🚀 Săn mã qua Quiz Game',
    '⭐ Tích điểm đổi coupon',
    '🎁 Mã giảm giá lên đến 50%',
    '🔥 Hạn sử dụng có giới hạn',
  ];

  constructor(private couponService: CouponService) {}

  ngOnInit(): void {
    this.loadCoupons();
  }

  ngOnDestroy(): void {
    clearInterval(this.tickInterval);
  }

  loadCoupons(): void {
    this.couponService.getAvailableCoupons().subscribe({
      next: res => {
        this.coupons = (res.result || []).filter(c => c.status === 'ACTIVE');
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  getDiscountLabel(c: CouponResponse): string {
    if (c.discountType === 'PERCENT') return `-${c.discountValue}%`;
    return `-${c.discountValue.toLocaleString('vi-VN')}đ`;
  }

  isExpiringSoon(c: CouponResponse): boolean {
    const end = new Date(c.endDate);
    const diff = end.getTime() - Date.now();
    return diff > 0 && diff < 3 * 24 * 60 * 60 * 1000; // < 3 ngày
  }

  getUsagePercent(c: CouponResponse): number {
    if (!c.usageLimit) return 0;
    return Math.min(100, Math.round((c.usedCount / c.usageLimit) * 100));
  }

  getRemainingCount(c: CouponResponse): number {
    return Math.max(0, c.usageLimit - c.usedCount);
  }

  isAlmostGone(c: CouponResponse): boolean {
    return this.getUsagePercent(c) >= 70;
  }
}