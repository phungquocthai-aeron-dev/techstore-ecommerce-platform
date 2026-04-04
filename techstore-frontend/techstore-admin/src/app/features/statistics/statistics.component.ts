import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf, DatePipe, DecimalPipe } from '@angular/common';

import { OrderService } from '../order/order.service';
import { WarehouseStatisticsService } from '../warehouse/warehouse-statistics.service';

import { ProductService } from '../product/product.service';
import { ProductListResponse } from '../product/models/product.model';
import { ProductSalesResponse } from '../order/models/order.model';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import {
  RevenueStatsResponse,
  TopVariantResponse,
  OrderSummaryResponse,
  TopLoyalCustomerResponse
} from '../order/models/order.model';
import { RevenuePeriod } from '../order/models/order-request.model';
import { InboundCostStatResponse, PeriodType } from '../warehouse/models/warehouse-statistics.model';

type TabType = 'revenue' | 'orders' | 'products' | 'customers' | 'warehouse' | 'product-sales';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.css'],
  standalone: true,
  imports: [NgFor, NgIf, FormsModule, DecimalPipe]
})
export class StatisticsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  activeTab: TabType = 'revenue';

  loading = false;

  // ─── Period ─────────────────────────────────
  selectedPeriod: RevenuePeriod = 'MONTH';
  fromDate = '';
  toDate   = '';

  periodOptions: { value: RevenuePeriod; label: string }[] = [
    { value: 'TODAY',   label: 'Hôm nay'    },
    { value: 'MONTH',   label: 'Tháng này'  },
    { value: 'QUARTER', label: 'Quý này'    },
    { value: 'YEAR',    label: 'Năm nay'    },
    { value: 'CUSTOM',  label: 'Tùy chọn'   },
  ];

  tabs: { key: TabType; label: string; icon: string }[] = [
    { key: 'revenue',   label: 'Doanh thu',    icon: 'bi-graph-up-arrow'    },
    { key: 'orders',    label: 'Đơn hàng',     icon: 'bi-bag-check-fill'    },
    { key: 'products',  label: 'Sản phẩm',     icon: 'bi-box-seam-fill'     },
    { key: 'customers', label: 'Khách hàng',   icon: 'bi-people-fill'       },
    { key: 'warehouse', label: 'Nhập kho',     icon: 'bi-building-fill'     },
    { key: 'product-sales', label: 'Doanh thu SP', icon: 'bi-graph-up' },
  ];

  // ─── Revenue ────────────────────────────────
  revenueStats: RevenueStatsResponse | null = null;
  revenueMax = 1;

  // ─── Orders ─────────────────────────────────
  orderSummary: OrderSummaryResponse | null = null;

  // ─── Products ───────────────────────────────
  topVariantsCount = 10;
  topVariants: TopVariantResponse[] = [];
  variantMax = 1;

  // ─── Customers ──────────────────────────────
  topCustomersCount = 10;
  topCustomers: TopLoyalCustomerResponse[] = [];

  // ─── Warehouse ──────────────────────────────
  warehouseStats: InboundCostStatResponse | null = null;
  warehouseMax = 1;

  readonly statusMap: Record<string, { label: string; css: string; color: string }> = {
    PENDING:    { label: 'Chờ xác nhận', css: 'pending',   color: '#f59e0b' },
    CONFIRMED:  { label: 'Đã xác nhận',  css: 'confirmed', color: '#0ea5e9' },
    SHIPPING:   { label: 'Đang giao',    css: 'shipping',  color: '#7c3aed' },
    DELIVERED:  { label: 'Đã giao',      css: 'delivered', color: '#10b981' },
    CANCELLED:  { label: 'Đã hủy',       css: 'cancelled', color: '#dc2626' },
    RETURNED:   { label: 'Hoàn hàng',    css: 'returned',  color: '#ec4899' },
  };

  productSearchQuery = '';
  productSearchResults: ProductListResponse[] = [];
  selectedProduct: ProductListResponse | null = null;
  productSalesData: ProductSalesResponse | null = null;
  productSalesLoading = false;
  productSalesMax = 1;
  showProductDropdown = false;
  private productSearch$ = new Subject<string>();

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private warehouseStatsService: WarehouseStatisticsService,
  ) {}

  ngOnInit(): void {
    this.loadAll();
    this.productSearch$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => this.productService.search({ keyword: q, page: 0, size: 10 }))
    ).pipe(takeUntil(this.destroy$)).subscribe(res => {
      this.productSearchResults = res.result?.content ?? [];
      this.showProductDropdown = true;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─── Load ────────────────────────────────────

  loadAll(): void {
    this.loading = true;

    const params = this.buildParams();

    forkJoin({
      revenue:     this.orderService.getRevenueStats(params),
      summary:     this.orderService.getOrderSummary({ ...params, status: 'ALL' }),
      topVariants: this.orderService.getTopVariants({ top: this.topVariantsCount, from: params.from, to: params.to }),
      topCustomers:this.orderService.getTopLoyalCustomers({ top: this.topCustomersCount, period: params.period, from: params.from, to: params.to }),
      warehouse:   this.warehouseStatsService.getInboundCost(this.mapPeriod(this.selectedPeriod), params.from, params.to),
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.revenueStats = res.revenue.result ?? null;
          const pts = this.revenueStats?.dataPoints ?? [];
          this.revenueMax   = Math.max(...pts.map(p => p.revenue), 1);

          this.orderSummary  = res.summary.result ?? null;

          this.topVariants  = res.topVariants.result ?? [];
          this.variantMax   = Math.max(...this.topVariants.map(v => v.totalQuantitySold), 1);

          this.topCustomers = res.topCustomers.result ?? [];

          this.warehouseStats = res.warehouse.result ?? null;
          const wpts = this.warehouseStats?.data ?? [];
          this.warehouseMax = Math.max(...wpts.map(p => p.totalCost), 1);

          this.loading = false;
        },
        error: () => { this.loading = false; }
      });

    if (this.selectedProduct) this.loadProductSales();
  }

  onPeriodChange(): void {
    if (this.selectedPeriod !== 'CUSTOM') this.loadAll();
  }

  applyCustomRange(): void {
    if (this.fromDate && this.toDate) this.loadAll();
  }

  switchTab(tab: TabType): void {
    this.activeTab = tab;
  }

  onProductSearchInput(): void {
    if (this.productSearchQuery.trim().length >= 1) {
      this.productSearch$.next(this.productSearchQuery.trim());
    } else {
      this.productSearchResults = [];
      this.showProductDropdown = false;
    }
  }

  selectProduct(p: ProductListResponse): void {
    this.selectedProduct = p;
    this.productSearchQuery = p.name;
    this.showProductDropdown = false;
    this.loadProductSales();
  }

  loadProductSales(): void {
    if (!this.selectedProduct) return;
    this.productSalesLoading = true;
    const params = this.buildParams();
    this.orderService.getProductSales(this.selectedProduct.id, params)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.productSalesData = res.result ?? null;
          const allQty = (this.productSalesData?.variants ?? [])
            .flatMap(v => v.dataPoints.map(d => d.quantitySold));
          this.productSalesMax = Math.max(...allQty, 1);
          this.productSalesLoading = false;
        },
        error: () => { this.productSalesLoading = false; }
      });
  }

  clearProductSelection(): void {
    this.selectedProduct = null;
    this.productSearchQuery = '';
    this.productSalesData = null;
    this.productSearchResults = [];
  }

  // ─── Helpers ────────────────────────────────

  private buildParams() {
    return {
      period: this.selectedPeriod,
      from:   this.fromDate || undefined,
      to:     this.toDate   || undefined,
    };
  }

  private mapPeriod(p: RevenuePeriod): PeriodType {
    const map: Record<RevenuePeriod, PeriodType> = {
      TODAY:   'TODAY',
      MONTH:   'MONTHLY',
      QUARTER: 'QUARTERLY',
      YEAR:    'YEARLY',
      CUSTOM:  'CUSTOM',
    };
    return map[p];
  }

  getBarH(val: number, max: number): number {
    return Math.max((val / max) * 100, 2);
  }

  getBarW(val: number, max: number): number {
    return Math.max((val / max) * 100, 2);
  }

  getStatusLabel(s: string): string { return this.statusMap[s]?.label ?? s; }
  getStatusCss(s: string):   string { return this.statusMap[s]?.css   ?? 'pending'; }
  getStatusColor(s: string): string { return this.statusMap[s]?.color ?? '#888'; }

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

  formatShort(val: number): string {
    if (val >= 1_000_000_000) return (val / 1_000_000_000).toFixed(1) + ' tỷ';
    if (val >= 1_000_000)     return (val / 1_000_000).toFixed(1) + ' tr';
    if (val >= 1_000)         return (val / 1_000).toFixed(0) + 'k';
    return val.toString();
  }

  formatCurrency(val: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
  }

  get totalRevenue(): number { return this.revenueStats?.totalRevenue ?? 0; }
  get totalOrderCount(): number { return this.revenueStats?.totalOrders ?? 0; }
  get avgOrderValue(): number { return this.totalOrderCount > 0 ? this.totalRevenue / this.totalOrderCount : 0; }
}