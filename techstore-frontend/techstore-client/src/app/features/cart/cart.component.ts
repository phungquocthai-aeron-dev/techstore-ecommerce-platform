import {
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil, forkJoin } from 'rxjs';

import { CartService } from './cart.service';
import { VariantService } from '../product/variant.service';
import { ProductService } from '../product/product.service';
import { CouponService } from '../coupon/coupon.service';

import { CartResponse, CartItemResponse } from './models/cart.model';
import { VariantResponse } from '../product/models/product.model';
import { CouponResponse } from '../coupon/models/coupon.model';

import { OrderItem } from '../check-out/check-out.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit, OnDestroy {

  cart: CartResponse | null = null;
  loading = true;

  variantMap = new Map<number, VariantResponse>();
  productNameMap = new Map<number, string>();
  selectedIds = new Set<number>();

  // ── Coupon ────────────────────────────────────────────────────────
  availableCoupons: CouponResponse[] = [];
  selectedCoupon: CouponResponse | null = null;
  couponsLoading = false;

  alertMsg = '';
  alertType: 'success' | 'warn' = 'success';
  private alertTimer: any;

  private destroy$ = new Subject<void>();

  constructor(
    private cartService: CartService,
    private variantService: VariantService,
    private productService: ProductService,
    private couponService: CouponService,
    private router: Router
  ) {}

  // ── Computed ──────────────────────────────────────────────────────

  get selectedItems(): CartItemResponse[] {
    return this.cart?.items.filter(i => this.selectedIds.has(i.variantId)) ?? [];
  }

  get selectedSubtotal(): number {
    return this.selectedItems.reduce((s, i) => s + i.subTotal, 0);
  }

  get discount(): number {
    if (!this.selectedCoupon) return 0;
    const c = this.selectedCoupon;
    if (this.selectedSubtotal < c.minOrderValue) return 0;
    let d = 0;
    if (c.discountType === 'PERCENT') {
      d = Math.round(this.selectedSubtotal * c.discountValue / 100);
    } else {
      // FIXED
      d = c.discountValue;
    }
    return Math.min(d, c.maxDiscount);
  }

  get grandTotal(): number {
    return Math.max(0, this.selectedSubtotal - this.discount);
  }

  get isAllSelected(): boolean {
    return !!this.cart && this.cart.items.length > 0 &&
      this.cart.items.every(i => this.selectedIds.has(i.variantId));
  }

  get isSomeSelected(): boolean {
    return this.selectedIds.size > 0;
  }

  get applicableCoupons(): CouponResponse[] {
    return this.availableCoupons.filter(c =>
      c.status === 'ACTIVE' &&
      this.selectedSubtotal >= c.minOrderValue &&
      new Date(c.endDate) >= new Date()
    );
  }

  get inapplicableCoupons(): CouponResponse[] {
    return this.availableCoupons.filter(c =>
      c.status === 'ACTIVE' &&
      this.selectedSubtotal < c.minOrderValue &&
      new Date(c.endDate) >= new Date()
    );
  }

  // ── Lifecycle ─────────────────────────────────────────────────────

  ngOnInit(): void {
    this.loadCart();
    this.loadCoupons();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    clearTimeout(this.alertTimer);
  }

  // ── Load cart ─────────────────────────────────────────────────────

  loadCart(): void {
    this.loading = true;
    this.cartService.getCart().pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.cart = res.result ?? null;
        if (this.cart && this.cart.items.length > 0) {
          this.loadVariantDetails();
        } else {
          this.loading = false;
        }
      },
      error: () => { this.loading = false; }
    });
  }

  private loadVariantDetails(): void {
    if (!this.cart) return;
    const variantIds = this.cart.items.map(i => i.variantId);

    this.variantService.getByIds(variantIds).pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        const variants = res.result ?? [];
        variants.forEach(v => this.variantMap.set(v.id, v));

        const productIds = [...new Set(variants.map(v => v.productId))];
        const requests = productIds.map(id => this.productService.getById(id));
        if (requests.length === 0) { this.loading = false; return; }

        forkJoin(requests).pipe(takeUntil(this.destroy$)).subscribe({
          next: results => {
            results.forEach(r => {
              const p = r.result;
              if (p) variants.filter(v => v.productId === p.id)
                             .forEach(v => this.productNameMap.set(v.id, p.name));
            });
            this.loading = false;
          },
          error: () => { this.loading = false; }
        });
      },
      error: () => { this.loading = false; }
    });
  }

  // ── Coupon ────────────────────────────────────────────────────────

  loadCoupons(): void {
    this.couponsLoading = true;
    this.couponService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.availableCoupons = (res.result ?? []).filter(
          c => c.status === 'ACTIVE' && new Date(c.endDate) >= new Date()
        );
        this.couponsLoading = false;
      },
      error: () => { this.couponsLoading = false; }
    });
  }

  selectCoupon(coupon: CouponResponse): void {
    if (this.selectedSubtotal < coupon.minOrderValue) return;
    this.selectedCoupon = this.selectedCoupon?.id === coupon.id ? null : coupon;
  }

  removeCoupon(): void {
    this.selectedCoupon = null;
  }

  calcCouponDiscount(c: CouponResponse): number {
    if (this.selectedSubtotal < c.minOrderValue) return 0;
    let d = c.discountType === 'PERCENT'
      ? Math.round(this.selectedSubtotal * c.discountValue / 100)
      : c.discountValue;
    return Math.min(d, c.maxDiscount);
  }

  applyBestCoupon(): void {
    if (this.applicableCoupons.length === 0) return;
    this.selectedCoupon = this.applicableCoupons.reduce((best, c) =>
      this.calcCouponDiscount(c) > this.calcCouponDiscount(best) ? c : best
    );
    this.showAlert(`Đã áp dụng mã "${this.selectedCoupon.name}"`, 'success');
  }

  getCouponStripeClass(c: CouponResponse): string {
    if (c.discountType === 'PERCENT') return 'blue';
    return 'green';
  }

  // ── Helpers ───────────────────────────────────────────────────────

  getVariantImage(variantId: number): string {
    return this.variantMap.get(variantId)?.imageUrl ?? 'assets/images/placeholder.png';
  }

  getVariantName(variantId: number): string {
    const v = this.variantMap.get(variantId);
    return v ? `${v.name} — ${v.color}` : `Variant #${variantId}`;
  }

  getProductName(variantId: number): string {
    return this.productNameMap.get(variantId) ?? `Sản phẩm #${variantId}`;
  }

  // ── Selection ─────────────────────────────────────────────────────

  toggleSelect(variantId: number): void {
    if (this.selectedIds.has(variantId)) this.selectedIds.delete(variantId);
    else this.selectedIds.add(variantId);
    this.selectedIds = new Set(this.selectedIds);
    if (this.selectedCoupon && this.selectedSubtotal < this.selectedCoupon.minOrderValue) {
      this.selectedCoupon = null;
      this.showAlert('Mã giảm giá đã bị hủy do đơn không đủ điều kiện', 'warn');
    }
  }

  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) this.cart?.items.forEach(i => this.selectedIds.add(i.variantId));
    else this.selectedIds.clear();
    this.selectedIds = new Set(this.selectedIds);
    if (this.selectedCoupon && this.selectedSubtotal < this.selectedCoupon.minOrderValue) {
      this.selectedCoupon = null;
    }
  }

  // ── Cart actions ──────────────────────────────────────────────────

  updateQty(variantId: number, newQty: number): void {
    if (newQty < 1 || newQty > 10) return;
    this.cartService.updateQuantity(variantId, { quantity: newQty })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const item = this.cart?.items.find(i => i.variantId === variantId);
          if (item) { item.quantity = newQty; item.subTotal = item.price * newQty; }
          this.showAlert('Đã cập nhật số lượng', 'success');
        },
        error: () => this.showAlert('Không thể cập nhật số lượng', 'warn')
      });
  }

  removeItem(variantId: number): void {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
    this.cartService.removeItem(variantId).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        if (this.cart) this.cart.items = this.cart.items.filter(i => i.variantId !== variantId);
        this.selectedIds.delete(variantId);
        this.selectedIds = new Set(this.selectedIds);
        this.showAlert('Đã xóa sản phẩm khỏi giỏ hàng', 'success');
      },
      error: () => this.showAlert('Không thể xóa sản phẩm', 'warn')
    });
  }

  removeSelected(): void {
    if (this.selectedIds.size === 0) return;
    if (!confirm(`Xóa ${this.selectedIds.size} sản phẩm đã chọn?`)) return;
    const ids = [...this.selectedIds];
    forkJoin(ids.map(id => this.cartService.removeItem(id))).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        if (this.cart) this.cart.items = this.cart.items.filter(i => !ids.includes(i.variantId));
        this.selectedIds.clear();
        this.selectedIds = new Set();
        this.showAlert(`Đã xóa ${ids.length} sản phẩm`, 'success');
      },
      error: () => this.showAlert('Có lỗi khi xóa sản phẩm', 'warn')
    });
  }

  // ── Navigate to order ─────────────────────────────────────────────

  goOrder(): void {
    if (this.selectedIds.size === 0) return;
    const orderItems: OrderItem[] = this.selectedItems.map(item => {
      const variant = this.variantMap.get(item.variantId);
      return {
        productId: variant?.productId ?? 0,
        variantId: item.variantId,
        productName: this.getProductName(item.variantId),
        variantName: this.getVariantName(item.variantId),
        imageUrl: this.getVariantImage(item.variantId),
        quantity: item.quantity,
        price: item.price,
        weight: variant?.weight ?? 300
      };
    });
    this.router.navigate(['/checkout'], {
      state: { items: orderItems, coupon: this.selectedCoupon }
    });
  }

  goShopping(): void {
    this.router.navigate(['/products']);
  }

  // ── Alert ─────────────────────────────────────────────────────────

  private showAlert(msg: string, type: 'success' | 'warn'): void {
    clearTimeout(this.alertTimer);
    this.alertMsg = msg;
    this.alertType = type;
    this.alertTimer = setTimeout(() => { this.alertMsg = ''; }, 3000);
  }
}