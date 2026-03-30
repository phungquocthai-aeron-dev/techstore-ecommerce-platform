import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, forkJoin } from 'rxjs';
import { NgFor, NgIf, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';

import { OrderService } from '../order/order.service';
import { CustomerService } from '../customer/customer.service';
import { ProductService } from '../product/product.service';

import {
  AdminOrderResponse,
  OrderSummaryResponse,
  TopVariantResponse,
  TopLoyalCustomerResponse,
  RevenueStatsResponse
} from '../order/models/order.model';


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: true,
  imports: [NgFor, NgIf, DatePipe]
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  loading = true;
  today = new Date();

  // ─── Stats ────────────────────────────────
  totalRevenue    = 0;
  totalOrders     = 0;
  totalCustomers  = 0;
  pendingOrders   = 0;

  revenueTrend    = 0;
  orderTrend      = 0;

  // ─── Order Summary ───────────────────────
  orderSummary: OrderSummaryResponse | null = null;

  // ─── Recent Orders ───────────────────────
  recentOrders: AdminOrderResponse[] = [];

  // ─── Top Variants ────────────────────────
  topVariants: TopVariantResponse[] = [];

  // ─── Top Loyal Customers ─────────────────
  topCustomers: TopLoyalCustomerResponse[] = [];

  // ─── Revenue chart data ───────────────────
  revenueStats: RevenueStatsResponse | null = null;
  chartMax = 1;

  // ─── Status map ──────────────────────────
  readonly statusMap: Record<string, { label: string; css: string }> = {
    PENDING:    { label: 'Chờ xác nhận', css: 'pending'   },
    CONFIRMED:  { label: 'Đã xác nhận',  css: 'confirmed' },
    SHIPPING:   { label: 'Đang giao',    css: 'shipping'  },
    DELIVERED:  { label: 'Đã giao',      css: 'delivered' },
    CANCELLED:  { label: 'Đã hủy',       css: 'cancelled' },
    RETURNED:   { label: 'Hoàn hàng',    css: 'returned'  },
  };

  constructor(
    private orderService: OrderService,
    private customerService: CustomerService,
    private productService: ProductService,
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDashboard(): void {
    this.loading = true;

    forkJoin({
      orders:      this.orderService.getAllOrders(),
      summary:     this.orderService.getOrderSummary(),
      revenue:     this.orderService.getRevenueStats({ period: 'MONTH' }),
      topVariants: this.orderService.getTopVariants({ top: 5 }),
      topCustomers:this.orderService.getTopLoyalCustomers({ top: 5 }),
      customers:   this.customerService.getAll(),
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const orders = res.orders.result ?? [];
          this.recentOrders    = orders.slice(0, 8);
          this.totalOrders     = orders.length;
          this.pendingOrders   = orders.filter(o => o.status === 'PENDING').length;

          this.orderSummary    = res.summary.result ?? null;
          this.totalRevenue    = res.summary.result?.totalRevenue ?? 0;

          this.topVariants     = res.topVariants.result ?? [];
          this.topCustomers    = res.topCustomers.result ?? [];
          this.totalCustomers  = (res.customers.result ?? []).length;

          this.revenueStats    = res.revenue.result ?? null;
          const pts = res.revenue.result?.dataPoints ?? [];
          this.chartMax        = Math.max(...pts.map(p => p.revenue), 1);

          this.loading = false;
        },
        error: () => { this.loading = false; }
      });
  }

  getStatusLabel(s: string): string {
    return this.statusMap[s]?.label ?? s;
  }

  getStatusCss(s: string): string {
    return this.statusMap[s]?.css ?? 'pending';
  }

  getBarHeight(rev: number): number {
    return Math.max((rev / this.chartMax) * 100, 3);
  }

  getInitial(name: string | null): string {
    if (!name?.trim()) return '?';
    const parts = name.trim().split(' ');
    return parts.length >= 2
      ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      : name[0].toUpperCase();
  }

  getAvatarBg(name: string | null): string {
    const colors = [
      'linear-gradient(135deg,#be123c,#fb7185)',
      'linear-gradient(135deg,#b45309,#fbbf24)',
      'linear-gradient(135deg,#0f766e,#34d399)',
      'linear-gradient(135deg,#1d4ed8,#60a5fa)',
      'linear-gradient(135deg,#7c3aed,#a78bfa)',
    ];
    let h = 0;
    for (let i = 0; i < (name?.length ?? 0); i++) h = (name!.charCodeAt(i)) + ((h << 5) - h);
    return colors[Math.abs(h) % colors.length];
  }

  formatCurrency(val: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
  }

  formatShort(val: number): string {
    if (val >= 1_000_000_000) return (val / 1_000_000_000).toFixed(1) + ' tỷ';
    if (val >= 1_000_000)     return (val / 1_000_000).toFixed(1) + ' tr';
    if (val >= 1_000)         return (val / 1_000).toFixed(0) + 'k';
    return val.toString();
  }

  getStatusBreakdown(status: string): number {
    return this.orderSummary?.statusBreakdown?.find(s => s.status === status)?.count ?? 0;
  }
}