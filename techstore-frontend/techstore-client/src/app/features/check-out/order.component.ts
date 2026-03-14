import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';

import { CustomerService } from '../user/customer.service';
import { AddressService } from '../user/address.service';
import { ShippingService } from './shipping.service';
import { OrderService } from './order.service';
import { PaymentMethodService } from './payment-method.service';

import { CustomerResponse } from '../user/models/customer.model';
import { AddressResponse } from '../user/models/address.model';
import { CouponResponse } from '../coupon/models/coupon.model';
import { PaymentMethodResponse } from './models/payment-method.model';
import { OrderCreateRequest, OrderItemRequest } from './models/order-request.model';

export interface OrderItem {
  productId: number;
  variantId: number;
  productName: string;
  variantName?: string;
  imageUrl: string;
  quantity: number;
  price: number;
  weight?: number;
}

@Component({
  selector: 'app-order',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './check-out.component.html',
  styleUrl: './check-out.component.css'
})
export class CheckoutComponent implements OnInit, OnDestroy {

  items: OrderItem[] = [];
  appliedCoupon: CouponResponse | null = null;

  currentUser: CustomerResponse | null = null;
  addresses: AddressResponse[] = [];
  paymentMethods: PaymentMethodResponse[] = [];

  selectedAddressId: number | null = null;
  selectedPaymentMethodId: number | null = null;

  orderError = '';

  shippingFee = 0;
  loading = true;
  loadingFee = false;
  placing = false;

  private readonly GHN_PROVIDER_ID = 1;
  private destroy$ = new Subject<void>();

  constructor(
    private customerService: CustomerService,
    private addressService: AddressService,
    private shippingService: ShippingService,
    private orderService: OrderService,
    private paymentMethodService: PaymentMethodService,
    private router: Router
  ) {}

  // ── Computed ──────────────────────────────────────────────────────

  get subtotal(): number {
    return this.items.reduce((s, i) => s + i.price * i.quantity, 0);
  }

  get discountAmount(): number {
    if (!this.appliedCoupon || this.subtotal < this.appliedCoupon.minOrderValue) return 0;
    if (this.appliedCoupon.discountType === 'PERCENT') {
      const raw = Math.round(this.subtotal * this.appliedCoupon.discountValue / 100);
      return Math.min(raw, this.appliedCoupon.maxDiscount);
    }
    return Math.min(this.appliedCoupon.discountValue, this.appliedCoupon.maxDiscount);
  }

  get total(): number {
    return Math.max(0, this.subtotal + this.shippingFee - this.discountAmount);
  }

  get canOrder(): boolean {
    return !!this.selectedAddressId && !!this.selectedPaymentMethodId && this.items.length > 0;
  }

  // ── Lifecycle ─────────────────────────────────────────────────────

  ngOnInit(): void {
    // Đọc state từ history.state (cách đúng sau khi navigation hoàn tất)
    const state = history.state;
    if (state?.items && Array.isArray(state.items)) {
      this.items = state.items;
    }
    if (state?.coupon) {
      this.appliedCoupon = state.coupon;
    }

    this.currentUser = this.customerService.currentUser;
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    forkJoin({
      addresses: this.addressService.getByCustomerId(this.currentUser.id),
      paymentMethods: this.paymentMethodService.getAllPaymentMethods(0, 50)
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ addresses, paymentMethods }) => {
        this.addresses = addresses.result ?? [];
        this.paymentMethods = (paymentMethods.result?.content ?? [])
          .filter(p => p.status === 'ACTIVE');

        if (this.addresses.length > 0) this.selectAddress(this.addresses[0]);
        if (this.paymentMethods.length > 0) this.selectedPaymentMethodId = this.paymentMethods[0].id;

        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Address & Shipping ────────────────────────────────────────────

  selectAddress(addr: AddressResponse): void {
    this.selectedAddressId = addr.id;
    this.fetchShippingFee(addr.id);
  }

  fetchShippingFee(addressId: number): void {
    const totalWeight = this.items.reduce((s, i) => s + (i.weight ?? 300) * i.quantity, 0);
    this.loadingFee = true;
    this.shippingService.calculateFee('ghn', addressId, totalWeight)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => { this.shippingFee = res.result ?? 0; this.loadingFee = false; },
        error: () => { this.shippingFee = 0; this.loadingFee = false; }
      });
  }

  // ── Place order ───────────────────────────────────────────────────

  placeOrder(): void {
    if (!this.canOrder || !this.currentUser) return;
    this.orderError = '';
    this.placing = true;

    const req: OrderCreateRequest = {
      customerId: this.currentUser.id,
      addressId: this.selectedAddressId!,
      paymentMethod: this.paymentMethods.find(p => p.id === this.selectedPaymentMethodId)?.name ?? '',
      paymentMethodId: this.selectedPaymentMethodId!,
      couponId: this.appliedCoupon?.id,
      shippingProviderId: this.GHN_PROVIDER_ID,
      items: this.items.map<OrderItemRequest>(i => ({
        variantId: i.variantId,
        quantity: i.quantity
      }))
    };

    this.orderService.createOrder(req).pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        const order = res.result;
        if (order?.paymentUrl) {
          window.location.href = order.paymentUrl;
        } else {
          this.router.navigate(['/order-success'], { queryParams: { orderId: order?.id } });
        }
      },
      error: err => {
        this.orderError = err?.error?.message ?? 'Đặt hàng thất bại. Vui lòng thử lại.';
        this.placing = false;
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────

  getPaymentIcon(name: string): string {
    const n = name.toLowerCase();
    if (n.includes('cod') || n.includes('tien mat')) return 'bi-cash-coin';
    if (n.includes('visa') || n.includes('card') || n.includes('the')) return 'bi-credit-card-fill';
    if (n.includes('bank') || n.includes('ngan hang') || n.includes('chuyen khoan')) return 'bi-bank';
    if (n.includes('momo')) return 'bi-phone';
    if (n.includes('vnpay') || n.includes('zalopay') || n.includes('qr')) return 'bi-qr-code';
    return 'bi-wallet2';
  }

  goBack(): void { this.router.navigate(['/cart']); }
  goAddAddress(): void { this.router.navigate(['/profile/addresses']); }
}