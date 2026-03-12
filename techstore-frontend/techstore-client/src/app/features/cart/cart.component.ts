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

import { CartResponse, CartItemResponse } from './models/cart.model';
import { VariantResponse } from '../product/models/product.model';

import { OrderItem } from '../order/order.component';

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

  /** Map variantId → VariantResponse để hiển thị tên/ảnh */
  variantMap = new Map<number, VariantResponse>();

  /** Map variantId → tên sản phẩm */
  productNameMap = new Map<number, string>();

  /** Set variantId đã được tick chọn */
  selectedIds = new Set<number>();

  alertMsg = '';
  alertType: 'success' | 'warn' = 'success';
  private alertTimer: any;

  private readonly FREE_SHIP_THRESHOLD = 30_000_000;
  private readonly SHIPPING_FEE_DEFAULT = 50_000;

  private destroy$ = new Subject<void>();

  constructor(
    private cartService: CartService,
    private variantService: VariantService,
    private productService: ProductService,
    private router: Router
  ) {}

  // ── Computed ──────────────────────────────────────────────────────

  get selectedItems(): CartItemResponse[] {
    return this.cart?.items.filter(i => this.selectedIds.has(i.variantId)) ?? [];
  }

  get selectedSubtotal(): number {
    return this.selectedItems.reduce((s, i) => s + i.subTotal, 0);
  }

  get shippingFee(): number {
    if (this.selectedIds.size === 0) return 0;
    return this.selectedSubtotal >= this.FREE_SHIP_THRESHOLD ? 0 : this.SHIPPING_FEE_DEFAULT;
  }

  get discount(): number {
    return 0; // voucher sẽ xử lý ở trang order
  }

  get grandTotal(): number {
    return Math.max(0, this.selectedSubtotal + this.shippingFee - this.discount);
  }

  get remainingForFreeShip(): number {
    if (this.selectedIds.size === 0) return this.FREE_SHIP_THRESHOLD;
    return Math.max(0, this.FREE_SHIP_THRESHOLD - this.selectedSubtotal);
  }

  get isAllSelected(): boolean {
    return !!this.cart && this.cart.items.length > 0 &&
      this.cart.items.every(i => this.selectedIds.has(i.variantId));
  }

  get isSomeSelected(): boolean {
    return this.selectedIds.size > 0;
  }

  // ── Lifecycle ─────────────────────────────────────────────────────

  ngOnInit(): void {
    this.loadCart();
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

  /** Tải thông tin variant (ảnh, tên variant, tên sản phẩm) cho từng item */
  private loadVariantDetails(): void {
    if (!this.cart) return;

    const variantIds = this.cart.items.map(i => i.variantId);

    this.variantService.getByIds(variantIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const variants = res.result ?? [];
          variants.forEach(v => this.variantMap.set(v.id, v));

          // Tải tên sản phẩm theo productId (unique)
          const productIds = [...new Set(variants.map(v => v.productId))];
          const requests = productIds.map(id => this.productService.getById(id));

          if (requests.length === 0) { this.loading = false; return; }

          forkJoin(requests).pipe(takeUntil(this.destroy$)).subscribe({
            next: results => {
              results.forEach(r => {
                const p = r.result;
                if (p) {
                  // Gán tên sản phẩm cho tất cả variant thuộc product này
                  variants
                    .filter(v => v.productId === p.id)
                    .forEach(v => this.productNameMap.set(v.id, p.name));
                }
              });
              this.loading = false;
            },
            error: () => { this.loading = false; }
          });
        },
        error: () => { this.loading = false; }
      });
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
    if (this.selectedIds.has(variantId)) {
      this.selectedIds.delete(variantId);
    } else {
      this.selectedIds.add(variantId);
    }
    this.selectedIds = new Set(this.selectedIds); // trigger CD
  }

  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.cart?.items.forEach(i => this.selectedIds.add(i.variantId));
    } else {
      this.selectedIds.clear();
    }
    this.selectedIds = new Set(this.selectedIds);
  }

  // ── Cart actions ──────────────────────────────────────────────────

  updateQty(variantId: number, newQty: number): void {
    if (newQty < 1 || newQty > 10) return;

    this.cartService.updateQuantity(variantId, { quantity: newQty })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const item = this.cart?.items.find(i => i.variantId === variantId);
          if (item) {
            item.quantity = newQty;
            item.subTotal = item.price * newQty;
          }
          this.showAlert('Đã cập nhật số lượng', 'success');
        },
        error: () => this.showAlert('Không thể cập nhật số lượng', 'warn')
      });
  }

  removeItem(variantId: number): void {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

    this.cartService.removeItem(variantId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          if (this.cart) {
            this.cart.items = this.cart.items.filter(i => i.variantId !== variantId);
          }
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
    const requests = ids.map(id => this.cartService.removeItem(id));

    forkJoin(requests).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        if (this.cart) {
          this.cart.items = this.cart.items.filter(i => !ids.includes(i.variantId));
        }
        this.selectedIds.clear();
        this.selectedIds = new Set();
        this.showAlert(`Đã xóa ${ids.length} sản phẩm`, 'success');
      },
      error: () => this.showAlert('Có lỗi khi xóa sản phẩm', 'warn')
    });
  }

  // ── Navigate to order ──────────────────────────────────────────

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

    // Truyền items qua router state
    this.router.navigate(['/order'], {
      state: { items: orderItems }
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