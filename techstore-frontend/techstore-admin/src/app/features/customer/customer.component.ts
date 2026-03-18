import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { CustomerService } from './customer.service';
import { Customer } from './models/customer.model';
import { CustomerUpdateRequest } from './models/customer-update.model';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';

interface ToastState {
  show:    boolean;
  message: string;
  type:    'success' | 'error';
}

@Component({
  selector:    'app-customer',
  templateUrl: './customer.component.html',
  styleUrls:   ['./customer.component.css'],
  standalone: true,
  imports: [FormsModule, NgFor, NgIf]
})
export class CustomerManagementComponent implements OnInit, OnDestroy {

  private destroy$      = new Subject<void>();
  private searchSubject = new Subject<string>();

  // ─── List ──────────────────────────────────────────────────────────────
  customers:    Customer[] = [];   // full list
  customerList: Customer[] = [];   // filtered for display
  loading       = false;

  // ─── Filter ────────────────────────────────────────────────────────────
  searchKeyword    = '';
  activeStatFilter = 'all';
  sortBy           = 'id';

  // ─── Counts ────────────────────────────────────────────────────────────
  totalCount    = 0;
  activeCount   = 0;
  inactiveCount = 0;
  googleCount   = 0;

  // ─── Toast ─────────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };
  saving = false;

  // ─── Detail modal ──────────────────────────────────────────────────────
  showDetailModal = false;
  detailTarget:   Customer | null = null;

  // ─── Edit modal ────────────────────────────────────────────────────────
  showEditModal = false;
  editTarget:   Customer | null = null;
  editForm: CustomerUpdateRequest = this.emptyEditForm();

  // ─── Status modal ──────────────────────────────────────────────────────
  showStatusModal = false;
  statusTarget:   Customer | null = null;

  constructor(private customerService: CustomerService) {}

  // ══════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initSearch();
    this.loadAll();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ══════════════════════════════════════════════════════════════════════
  // DATA LOADING
  // ══════════════════════════════════════════════════════════════════════

  private initSearch(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(kw => this.applyFilters(kw));
  }

  loadAll(): void {
    this.loading = true;
    this.customerService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.customers = res.result ?? [];
          this.calcCounts();
          this.applyFilters(this.searchKeyword);
          this.loading = false;
        },
        error: () => {
          this.showToast('Không thể tải danh sách khách hàng', 'error');
          this.loading = false;
        }
      });
  }

  private calcCounts(): void {
    this.totalCount    = this.customers.length;
    this.activeCount   = this.customers.filter(c => c.status === 'ACTIVE').length;
    this.inactiveCount = this.customers.filter(c => c.status !== 'ACTIVE').length;
    this.googleCount   = this.customers.filter(c => c.provider === 'GOOGLE').length;
  }

  private applyFilters(kw: string): void {
    let list = [...this.customers];

    // Keyword filter
    if (kw.trim()) {
      const k = kw.toLowerCase();
      list = list.filter(c =>
        c.fullName?.toLowerCase().includes(k) ||
        c.email?.toLowerCase().includes(k) ||
        c.phone?.includes(k) ||
        String(c.id).includes(k)
      );
    }

    // Stat filter
    if (this.activeStatFilter === 'ACTIVE') {
      list = list.filter(c => c.status === 'ACTIVE');
    } else if (this.activeStatFilter === 'INACTIVE') {
      list = list.filter(c => c.status !== 'ACTIVE');
    } else if (this.activeStatFilter === 'GOOGLE') {
      list = list.filter(c => c.provider === 'GOOGLE');
    }

    // Sort
    if (this.sortBy === 'name') {
      list = [...list].sort((a, b) => (a.fullName ?? '').localeCompare(b.fullName ?? '', 'vi'));
    }

    this.customerList = list;
  }

  // ══════════════════════════════════════════════════════════════════════
  // FILTERS
  // ══════════════════════════════════════════════════════════════════════

  onSearchChange(): void { this.searchSubject.next(this.searchKeyword); }

  clearSearch(): void {
    this.searchKeyword = '';
    this.applyFilters('');
  }

  filterByStat(filter: string): void {
    this.activeStatFilter = filter;
    this.applyFilters(this.searchKeyword);
  }

  applySortFilter(): void {
    this.applyFilters(this.searchKeyword);
  }

  // ══════════════════════════════════════════════════════════════════════
  // VIEW DETAIL
  // ══════════════════════════════════════════════════════════════════════

  viewCustomer(c: Customer): void {
    this.detailTarget   = c;
    this.showDetailModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════
  // EDIT
  // ══════════════════════════════════════════════════════════════════════

  editCustomer(c: Customer): void {
    this.editTarget = c;
    this.editForm   = {
      fullName: c.fullName,
      phone:    c.phone   ?? '',
      dob:      c.dob     ?? ''
    };
    this.showEditModal = true;
  }

  closeEditModal(): void { this.showEditModal = false; }

  saveCustomer(): void {
    if (!this.editTarget) return;
    if (!this.editForm.fullName?.trim()) {
      this.showToast('Vui lòng nhập họ và tên', 'error'); return;
    }

    this.saving = true;

    const req: CustomerUpdateRequest = {
      fullName: this.editForm.fullName?.trim() || undefined,
      phone:    this.editForm.phone            || undefined,
      dob:      this.editForm.dob              || undefined
    };

    this.customerService.updateInfo(this.editTarget.id, req)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.saving        = false;
          this.showEditModal = false;
          // Update local
          const cust = this.customers.find(c => c.id === this.editTarget!.id);
          if (cust && res.result) Object.assign(cust, res.result);
          this.applyFilters(this.searchKeyword);
          this.showToast('Cập nhật thông tin thành công!', 'success');
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Lỗi cập nhật', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // STATUS
  // ══════════════════════════════════════════════════════════════════════

  openStatusModal(c: Customer): void {
    this.statusTarget   = c;
    this.showStatusModal = true;
  }

  executeToggleStatus(): void {
    if (!this.statusTarget) return;
    this.saving = true;
    const newStatus = this.nextStatus(this.statusTarget.status);

    this.customerService.updateStatus(this.statusTarget.id, newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving           = false;
          this.showStatusModal  = false;
          // Update local
          const cust = this.customers.find(c => c.id === this.statusTarget!.id);
          if (cust) cust.status = newStatus;
          this.calcCounts();
          this.applyFilters(this.searchKeyword);
          this.showToast(
            newStatus === 'ACTIVE' ? 'Đã kích hoạt tài khoản!' : 'Đã tạm khóa tài khoản!',
            'success'
          );
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Lỗi cập nhật trạng thái', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════════════════

  getStatusLabel(status: string): string {
    return { ACTIVE: 'Đang hoạt động', INACTIVE: 'Tạm khóa', BANNED: 'Bị cấm' }[status] ?? status;
  }

  nextStatus(status: string): string {
    return status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleDateString('vi-VN', {
        day: '2-digit', month: '2-digit', year: 'numeric'
      });
    } catch { return dateStr; }
  }

  getInitial(name: string): string {
    if (!name?.trim()) return '?';
    const parts = name.trim().split(' ');
    return parts.length >= 2
      ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      : name[0].toUpperCase();
  }

  getAvatarBg(name: string): string {
    const colors = [
      'linear-gradient(135deg,#be123c,#fb7185)',
      'linear-gradient(135deg,#b45309,#fbbf24)',
      'linear-gradient(135deg,#0f766e,#34d399)',
      'linear-gradient(135deg,#1d4ed8,#60a5fa)',
      'linear-gradient(135deg,#7c3aed,#a78bfa)',
      'linear-gradient(135deg,#0e7490,#38bdf8)',
      'linear-gradient(135deg,#be185d,#f472b6)',
    ];
    let h = 0;
    for (let i = 0; i < (name?.length ?? 0); i++) h = name.charCodeAt(i) + ((h << 5) - h);
    return colors[Math.abs(h) % colors.length];
  }

  private emptyEditForm(): CustomerUpdateRequest {
    return { fullName: '', phone: '', dob: '' };
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { show: true, message, type };
    setTimeout(() => this.toast.show = false, 3000);
  }
}