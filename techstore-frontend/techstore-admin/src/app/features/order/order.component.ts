import {
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';

import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { OrderService } from './order.service';
import { StaffService } from '../staff/staff.service';

import { CustomerOrderItemResponse, CustomerOrderResponse } from './models/order.model';
import { DecimalPipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

// ─── AdminOrderResponse (mirrors BE AdminOrderResponse.java) ────────────────
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
  createdAt:            string;          // LocalDateTime serialised as string
  expectedDeliveryTime: string;
  shippingProviderName: string;
  couponName?:          string;
  address:              string;
  items:                CustomerOrderItemResponse[];
}

// ─── Local alias ────────────────────────────────────────────────────────────
type OrderUI = AdminOrderResponse;

// ─── Other types ─────────────────────────────────────────────────────────────
type OrderStatus =
  | 'PROCESSING'
  | 'READY_TO_SHIP'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'RETURNED';

type PendingAction = 'confirm' | 'cancel' | 'deliver' | 'return';

interface StatusTab {
  value:   string;
  label:   string;
  icon:    string;
  color:   string;
  bgColor: string;
  count:   number;
}

interface ActionMeta {
  title:        string;
  description:  string;
  icon:         string;
  iconBg:       string;
  color:        string;
  btnClass:     string;
  confirmLabel: string;
  targetStatus: string;
}

interface ToastState {
  show:    boolean;
  message: string;
  type:    'success' | 'error';
}

@Component({
  selector:    'app-order',
  templateUrl: './order.component.html',
  styleUrls:   ['./order.component.css'],
  standalone: true,
  imports: [DecimalPipe, NgIf, FormsModule, NgFor]
})
export class OrderManagementComponent implements OnInit, OnDestroy {

  private destroy$      = new Subject<void>();
  private searchSubject = new Subject<string>();

  // ─── List state ─────────────────────────────────────────────────────────
  orders:  OrderUI[] = [];
  loading  = false;

  // ─── Filter / sort ───────────────────────────────────────────────────────
  searchKeyword = '';
  sortBy        = 'id';
  activeTab     = 'ALL';

  // ─── Pagination ──────────────────────────────────────────────────────────
  currentPage   = 0;
  pageSize      = 15;
  totalPages    = 0;
  totalElements = 0;
  pageNumbers:  number[] = [];

  // ─── Checkbox ────────────────────────────────────────────────────────────
  allChecked = false;
  checkedIds = new Set<number>();

  // ─── Toast ───────────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };

  // ─── Timeline steps (ordered) ────────────────────────────────────────────
  timelineSteps: string[] = [
    'PROCESSING',
    'READY_TO_SHIP',
    // 'SHIPPING',
    'DELIVERED'
  ];

  // ─── Status tabs ─────────────────────────────────────────────────────────
  statusTabs: StatusTab[] = [
    { value: 'ALL',          label: 'Tất cả',         icon: 'bi-grid-fill',           color: '#0ea5e9', bgColor: '#f0f9ff', count: 0 },
    { value: 'PROCESSING',   label: 'Chờ xác nhận',   icon: 'bi-hourglass-split',     color: '#f59e0b', bgColor: '#fffbeb', count: 0 },
    { value: 'READY_TO_SHIP',label: 'Đang giao hàng',  icon: 'bi-box-seam-fill',       color: '#6366f1', bgColor: '#eef2ff', count: 0 },
    // { value: 'SHIPPING',     label: 'Đang giao',      icon: 'bi-truck',               color: '#0ea5e9', bgColor: '#f0f9ff', count: 0 },
    { value: 'DELIVERED',    label: 'Đã giao',        icon: 'bi-patch-check-fill',    color: '#10b981', bgColor: '#ecfdf5', count: 0 },
    { value: 'CANCELLED',    label: 'Đã hủy',         icon: 'bi-x-circle-fill',       color: '#ef4444', bgColor: '#fef2f2', count: 0 },
    { value: 'RETURNED',     label: 'Hoàn trả',       icon: 'bi-arrow-return-left',   color: '#8b5cf6', bgColor: '#f5f3ff', count: 0 },
  ];

  // ─── Detail modal ────────────────────────────────────────────────────────
  showDetailModal = false;
  selectedOrder:  OrderUI | null = null;

  // ─── Action modal ────────────────────────────────────────────────────────
  showActionModal = false;
  actionTarget:   OrderUI | null = null;
  pendingAction:  PendingAction | null = null;
  actionNote      = '';
  saving          = false;

  actionMeta: ActionMeta = this.buildActionMeta('confirm', 'PROCESSING');

  // ─── Staff ───────────────────────────────────────────────────────────────
  get staffId(): number {
    return this.staffService.currentStaff?.id ?? 0;
  }

  constructor(
    private orderService: OrderService,
    private staffService: StaffService
  ) {}
  // NOTE: OrderService phải có method:
  //   getAllOrders(status?: string): Observable<ApiResponse<AdminOrderResponse[]>>
  // map tới: GET /orders?status=... (admin endpoint)

  // ══════════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initSearch();
    this.loadOrders();
    this.loadCountsForTabs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DATA LOADING
  // ══════════════════════════════════════════════════════════════════════════

  private initSearch(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadOrders();
    });
  }

  loadOrders(): void {
    this.loading = true;
    const status = this.activeTab !== 'ALL' ? this.activeTab : undefined;

    this.orderService.getAllOrders(status)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          console.log(res)
          let list: OrderUI[] = (res.result ?? []) as unknown as OrderUI[];

          // Client-side keyword filter across all searchable fields
          if (this.searchKeyword.trim()) {
            const kw = this.searchKeyword.toLowerCase();
            list = list.filter(o =>
              String(o.orderId).includes(kw) ||
              o.shippingCode?.toLowerCase().includes(kw) ||
              o.customerName?.toLowerCase().includes(kw) ||
              o.customerEmail?.toLowerCase().includes(kw) ||
              o.customerPhone?.includes(kw) ||
              o.address?.toLowerCase().includes(kw) ||
              o.items?.some(i => i.name?.toLowerCase().includes(kw))
            );
          }

          // Sort
          if (this.sortBy === 'totalPrice') {
            list = [...list].sort((a, b) => b.totalPrice - a.totalPrice);
          }

          this.totalElements = list.length;
          this.totalPages    = Math.ceil(list.length / this.pageSize) || 1;
          const start = this.currentPage * this.pageSize;
          this.orders = list.slice(start, start + this.pageSize);
          this.buildPageNumbers();
          this.loading = false;
        },
        error: () => {
          this.showToast('Không thể tải danh sách đơn hàng', 'error');
          this.loading = false;
        }
      });
  }

  /** Load counts for each status tab from a single full-list call */
  loadCountsForTabs(): void {
    this.orderService.getAllOrders()
      .pipe(takeUntil(this.destroy$))
      .subscribe(r => {
        const all = (r.result ?? []) as unknown as OrderUI[];
        const tabAll = this.statusTabs.find(t => t.value === 'ALL');
        if (tabAll) tabAll.count = all.length;

        const statuses: OrderStatus[] = ['PROCESSING','READY_TO_SHIP','DELIVERED','CANCELLED','RETURNED'];
        statuses.forEach(s => {
          const t = this.statusTabs.find(x => x.value === s);
          if (t) t.count = all.filter(o => o.status === s).length;
        });
      });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // FILTERS & TABS
  // ══════════════════════════════════════════════════════════════════════════

  switchTab(tab: string): void {
    this.activeTab   = tab;
    this.currentPage = 0;
    this.loadOrders();
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchKeyword);
  }

  clearSearch(): void {
    this.searchKeyword = '';
    this.currentPage   = 0;
    this.loadOrders();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // PAGINATION
  // ══════════════════════════════════════════════════════════════════════════

  goPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.loadOrders();
  }

  private buildPageNumbers(): void {
    const pages: number[] = [];
    const total = this.totalPages;
    const cur   = this.currentPage;
    if (total <= 7) {
      for (let i = 0; i < total; i++) pages.push(i);
    } else {
      pages.push(0);
      if (cur > 3) pages.push(-1);
      for (let i = Math.max(1, cur-1); i <= Math.min(total-2, cur+1); i++) pages.push(i);
      if (cur < total-4) pages.push(-1);
      pages.push(total-1);
    }
    this.pageNumbers = pages;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // CHECKBOX
  // ══════════════════════════════════════════════════════════════════════════

  toggleAll(): void {
    if (this.allChecked) {
      this.orders.forEach(o => this.checkedIds.add(o.orderId));
    } else {
      this.orders.forEach(o => this.checkedIds.delete(o.orderId));
    }
  }

  toggleCheck(id: number): void {
    if (this.checkedIds.has(id)) this.checkedIds.delete(id);
    else this.checkedIds.add(id);
    this.allChecked = this.orders.every(o => this.checkedIds.has(o.orderId));
  }

  // ══════════════════════════════════════════════════════════════════════════
  // VIEW DETAIL
  // ══════════════════════════════════════════════════════════════════════════

  viewOrder(o: OrderUI): void {
    this.selectedOrder  = o;
    this.showDetailModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // PRINT LABEL
  // ══════════════════════════════════════════════════════════════════════════

  printLabel(o: OrderUI): void {
    this.orderService.printLabel(o.orderId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const url = res.result;
          if (url) window.open(url, '_blank');
          else this.showToast('Không có URL nhãn in', 'error');
        },
        error: () => this.showToast('Không thể lấy nhãn in', 'error')
      });
  }

  printSelected(): void {
    this.checkedIds.forEach(id => {
      this.orderService.printLabel(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe(res => {
          if (res.result) window.open(res.result, '_blank');
        });
    });
  }

  exportExcel(): void {
    this.showToast('Chức năng xuất Excel đang phát triển', 'success');
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ACTIONS
  // ══════════════════════════════════════════════════════════════════════════

  /** Open from table row buttons */
  openConfirmAction(o: OrderUI, action: PendingAction): void {
    this.actionTarget   = o;
    this.pendingAction  = action;
    this.actionNote     = '';
    this.actionMeta     = this.buildActionMeta(action, o.status as OrderStatus);
    this.showActionModal = true;
  }

  /** Open from inside detail modal */
  openConfirmActionFromDetail(action: PendingAction): void {
    if (!this.selectedOrder) return;
    this.openConfirmAction(this.selectedOrder, action);
  }

  executeAction(): void {
    if (!this.actionTarget || !this.pendingAction) return;
    const id = this.actionTarget.orderId;
    this.saving = true;

    let obs$;

    switch (this.pendingAction) {
      case 'confirm':
        obs$ = this.orderService.confirmOrder(id, this.staffId);
        break;
      case 'cancel':
        obs$ = this.orderService.cancelOrder(id, this.staffId);
        break;
      case 'deliver':
        obs$ = this.orderService.updateStatus(id, 'DELIVERED');
        break;
      case 'return':
        obs$ = this.orderService.updateStatus(id, 'RETURNED');
        break;
    }

    obs$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving = false;
        this.showActionModal = false;

        // Update status locally for instant feedback
        const newStatus = this.actionMeta.targetStatus;
        const order = this.orders.find(o => o.orderId === id);
        if (order) order.status = newStatus;
        if (this.selectedOrder?.orderId === id) this.selectedOrder.status = newStatus;

        this.showToast(this.actionMeta.title + ' thành công!', 'success');
        this.loadCountsForTabs();
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Đã xảy ra lỗi', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════════════════════

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PROCESSING:    'Chờ xác nhận',
      READY_TO_SHIP: 'Đang giao',
      SHIPPING:      'Đang giao',
      DELIVERED:     'Đã giao',
      CANCELLED:     'Đã hủy',
      RETURNED:      'Hoàn trả',
      DISABLE:       'Đã vô hiệu'
    };
    return map[status] ?? status;
  }

  isStepDone(currentStatus: string, step: string): boolean {
    const order = this.timelineSteps;
    const curIdx  = order.indexOf(currentStatus);
    const stepIdx = order.indexOf(step);
    if (curIdx === -1) return false;
    return stepIdx < curIdx;
  }

  getStepIcon(step: string): string {
    return {
      PROCESSING:    'bi-hourglass-split',
      READY_TO_SHIP: 'bi-box-seam',
      // SHIPPING:      'bi-truck',
      DELIVERED:     'bi-patch-check-fill'
    }[step] ?? 'bi-circle';
  }

  canAct(status: string): boolean {
    return ['PROCESSING', 'READY_TO_SHIP', 'SHIPPING'].includes(status);
  }

  getInitial(name: string): string {
    if (!name?.trim()) return '?';
    const parts = name.trim().split(' ');
    // Lấy chữ cái đầu của từ cuối (họ Việt Nam thường đặt tên ở cuối)
    return parts[parts.length - 1][0]?.toUpperCase() ?? '?';
  }

  truncate(text: string, max: number): string {
    if (!text) return '';
    return text.length > max ? text.slice(0, max) + '…' : text;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch { return dateStr; }
  }

  formatTime(dateStr: string): string {
    if (!dateStr) return '';
    try {
      const d = new Date(dateStr);
      return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    } catch { return ''; }
  }

  getSubtotal(order: CustomerOrderResponse): number {
    return (order.items ?? []).reduce((sum, i) => sum + i.price * i.quantity, 0);
  }

  // ── Build action metadata ────────────────────────────────────────────────
  private buildActionMeta(action: PendingAction, fromStatus: OrderStatus): ActionMeta {
    const map: Record<PendingAction, ActionMeta> = {
      confirm: {
        title:        'Xác nhận đơn hàng',
        description:  'Xác nhận đơn hàng và chuyển sang trạng thái Sẵn sàng giao. Nhà vận chuyển sẽ đến lấy hàng.',
        icon:         'bi-check2-circle',
        iconBg:       '#eef2ff',
        color:        '#6366f1',
        btnClass:     'om-btn-confirm',
        confirmLabel: 'Xác nhận đơn',
        targetStatus: 'READY_TO_SHIP'
      },
      cancel: {
        title:        'Hủy đơn hàng',
        description:  'Hủy đơn hàng này. Tồn kho sẽ được hoàn lại. Vui lòng nhập lý do để thông báo khách hàng.',
        icon:         'bi-x-circle',
        iconBg:       '#fef2f2',
        color:        '#ef4444',
        btnClass:     'om-btn-cancel',
        confirmLabel: 'Hủy đơn',
        targetStatus: 'CANCELLED'
      },
      deliver: {
        title:        'Xác nhận đã giao hàng',
        description:  'Xác nhận đơn hàng đã được giao thành công đến khách hàng.',
        icon:         'bi-patch-check',
        iconBg:       '#ecfdf5',
        color:        '#10b981',
        btnClass:     'om-btn-deliver',
        confirmLabel: 'Xác nhận đã giao',
        targetStatus: 'DELIVERED'
      },
      return: {
        title:        'Xử lý hoàn trả',
        description:  'Ghi nhận đơn hàng hoàn trả. Vui lòng nhập lý do để lưu vào hệ thống.',
        icon:         'bi-arrow-return-left',
        iconBg:       '#f5f3ff',
        color:        '#8b5cf6',
        btnClass:     'om-btn-return',
        confirmLabel: 'Xác nhận hoàn trả',
        targetStatus: 'RETURNED'
      }
    };
    return map[action];
  }

  // ── Toast ────────────────────────────────────────────────────────────────
  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { show: true, message, type };
    setTimeout(() => this.toast.show = false, 3000);
  }
}