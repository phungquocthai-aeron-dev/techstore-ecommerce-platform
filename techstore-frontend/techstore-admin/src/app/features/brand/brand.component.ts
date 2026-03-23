import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { BrandService } from '../product/brand.service';
import { BrandResponse } from '../product/models/brand.model';
import { BrandCreateRequest, BrandUpdateRequest } from '../product/models/brand-request.model';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { PermissionService } from '../../core/services/permission.service';


interface BrandForm {
  name:   string;
  status: string;
}

interface ToastState {
  show:    boolean;
  message: string;
  type:    'success' | 'error';
}

@Component({
  selector:    'app-brand',
  templateUrl: './brand.component.html',
  styleUrls:   ['./brand.component.css'],
  standalone: true,
  imports: [FormsModule, NgIf, NgFor]
})
export class BrandManagementComponent implements OnInit, OnDestroy {

  private destroy$      = new Subject<void>();
  private searchSubject = new Subject<string>();

  // ─── List ──────────────────────────────────────────────────────────────
  brands:   BrandResponse[] = [];
  loading   = false;
  viewMode: 'table' | 'grid' = 'table';

  // ─── Filter / sort ─────────────────────────────────────────────────────
  searchKeyword = '';
  filterStatus  = '';
  sortBy        = 'id';
  sortDirection = 'DESC';

  // ─── Counts (for stat cards) ────────────────────────────────────────────
  totalCount    = 0;
  activeCount   = 0;
  inactiveCount = 0;

  // ─── Pagination ────────────────────────────────────────────────────────
  currentPage   = 0;
  pageSize      = 12;
  totalPages    = 0;
  totalElements = 0;
  pageNumbers:  number[] = [];

  // ─── Checkbox ──────────────────────────────────────────────────────────
  allChecked = false;
  checkedIds = new Set<number>();

  // ─── Status options ────────────────────────────────────────────────────
  statusOptions = [
    { value: 'ACTIVE',   label: 'Đang hoạt động' },
    { value: 'INACTIVE', label: 'Tạm ngừng'       }
  ];

  // ─── Toast ─────────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };

  // ─── Form modal ────────────────────────────────────────────────────────
  showFormModal  = false;
  isEdit         = false;
  saving         = false;
  editingId: number | null = null;
  form: BrandForm = this.emptyForm();

  // ─── Status toggle modal ───────────────────────────────────────────────
  showStatusModal = false;
  statusTarget:   BrandResponse | null = null;

  // ─── Delete modal ──────────────────────────────────────────────────────
  showDeleteModal = false;
  deleteTarget:   BrandResponse | null = null;

  constructor(
    private brandService: BrandService,
    public perm: PermissionService
  ) {}

  // ══════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initSearch();
    this.loadBrands();
    this.loadCounts();
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
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadBrands();
    });
  }

  loadBrands(): void {
    this.loading = true;

    const hasFilter = this.searchKeyword.trim() || this.filterStatus;

    const obs = hasFilter
      ? this.brandService.searchBrands(
          this.searchKeyword.trim() || undefined,
          this.filterStatus          || undefined,
          this.currentPage,
          this.pageSize,
          this.sortBy,
          this.sortDirection
        )
      : this.filterStatus
        ? this.brandService.getBrandsByStatus(
            this.filterStatus,
            this.currentPage,
            this.pageSize,
            this.sortBy,
            this.sortDirection
          )
        : this.brandService.getAllBrands(
            this.currentPage,
            this.pageSize,
            this.sortBy,
            this.sortDirection
          );

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        const page = res.result;
        this.brands        = page?.content      ?? [];
        this.totalElements = page?.totalElements ?? 0;
        this.totalPages    = page?.totalPages    ?? 0;
        this.buildPageNumbers();
        this.loading = false;
      },
      error: () => {
        this.showToast('Không thể tải thương hiệu', 'error');
        this.loading = false;
      }
    });
  }

  /** Load counts from getAllBrands (full) */
  loadCounts(): void {
    this.brandService.getAllBrands(0, 999)
      .pipe(takeUntil(this.destroy$))
      .subscribe(res => {
        const all = res.result?.content ?? [];
        this.totalCount    = all.length;
        this.activeCount   = all.filter(b => b.status === 'ACTIVE').length;
        this.inactiveCount = all.filter(b => b.status === 'INACTIVE').length;
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // FILTERS
  // ══════════════════════════════════════════════════════════════════════

  onSearchChange(): void { this.searchSubject.next(this.searchKeyword); }

  clearSearch(): void {
    this.searchKeyword = '';
    this.currentPage   = 0;
    this.loadBrands();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadBrands();
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.currentPage  = 0;
    this.loadBrands();
  }

  // ══════════════════════════════════════════════════════════════════════
  // PAGINATION
  // ══════════════════════════════════════════════════════════════════════

  goPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.loadBrands();
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

  // ══════════════════════════════════════════════════════════════════════
  // CHECKBOX
  // ══════════════════════════════════════════════════════════════════════

  toggleAll(): void {
    if (this.allChecked) this.brands.forEach(b => this.checkedIds.add(b.id));
    else this.brands.forEach(b => this.checkedIds.delete(b.id));
  }

  toggleCheck(id: number): void {
    if (this.checkedIds.has(id)) this.checkedIds.delete(id);
    else this.checkedIds.add(id);
    this.allChecked = this.brands.every(b => this.checkedIds.has(b.id));
  }

  // ══════════════════════════════════════════════════════════════════════
  // CREATE
  // ══════════════════════════════════════════════════════════════════════

  openCreateModal(): void {
    this.isEdit        = false;
    this.editingId     = null;
    this.form          = this.emptyForm();
    this.showFormModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════
  // EDIT
  // ══════════════════════════════════════════════════════════════════════

  editBrand(b: BrandResponse): void {
    this.isEdit    = true;
    this.editingId = b.id;
    this.form      = { name: b.name, status: b.status };
    this.showFormModal = true;
  }

  closeFormModal(): void { this.showFormModal = false; }

  saveBrand(): void {
    if (!this.form.name?.trim()) {
      this.showToast('Vui lòng nhập tên thương hiệu', 'error'); return;
    }

    this.saving = true;

    const obs = this.isEdit && this.editingId != null
      ? this.brandService.updateBrand(this.editingId, {
          name:   this.form.name.trim(),
          status: this.form.status
        } as BrandUpdateRequest)
      : this.brandService.createBrand({
          name:   this.form.name.trim(),
          status: this.form.status
        } as BrandCreateRequest);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving        = false;
        this.showFormModal = false;
        this.showToast(
          this.isEdit ? 'Cập nhật thương hiệu thành công!' : 'Thêm thương hiệu thành công!',
          'success'
        );
        this.loadBrands();
        this.loadCounts();
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Đã xảy ra lỗi', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════
  // TOGGLE STATUS
  // ══════════════════════════════════════════════════════════════════════

  toggleStatus(b: BrandResponse): void {
    this.statusTarget   = b;
    this.showStatusModal = true;
  }

  executeToggleStatus(): void {
    if (!this.statusTarget) return;
    this.saving = true;

    const newStatus = this.nextStatus(this.statusTarget.status);

    this.brandService.updateStatus(this.statusTarget.id, newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving          = false;
          this.showStatusModal = false;
          // Update locally
          const brand = this.brands.find(b => b.id === this.statusTarget!.id);
          if (brand) brand.status = newStatus;
          this.showToast('Cập nhật trạng thái thành công!', 'success');
          this.loadCounts();
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Lỗi cập nhật trạng thái', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // DELETE
  // ══════════════════════════════════════════════════════════════════════

  confirmDelete(b: BrandResponse): void {
    this.deleteTarget   = b;
    this.showDeleteModal = true;
  }

  executeDelete(): void {
    if (!this.deleteTarget) return;
    this.saving = true;

    this.brandService.deleteBrand(this.deleteTarget.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving          = false;
          this.showDeleteModal = false;
          this.showToast('Đã xóa thương hiệu!', 'success');
          this.loadBrands();
          this.loadCounts();
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Không thể xóa thương hiệu', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════════════════

  getStatusLabel(status: string): string {
    return { ACTIVE: 'Đang hoạt động', INACTIVE: 'Tạm ngừng' }[status] ?? status;
  }

  nextStatus(status: string): string {
    return status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  }

  getInitial(name: string): string {
    if (!name?.trim()) return '?';
    return name.trim().split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
  }

  /** Generate a deterministic pastel-dark gradient colour from brand name */
  getBrandBg(name: string): string {
    const palettes = [
      'linear-gradient(135deg,#7c3aed,#a855f7)',
      'linear-gradient(135deg,#1d4ed8,#3b82f6)',
      'linear-gradient(135deg,#0f766e,#14b8a6)',
      'linear-gradient(135deg,#b45309,#f59e0b)',
      'linear-gradient(135deg,#be185d,#ec4899)',
      'linear-gradient(135deg,#1e40af,#60a5fa)',
      'linear-gradient(135deg,#065f46,#34d399)',
      'linear-gradient(135deg,#7f1d1d,#f87171)',
      'linear-gradient(135deg,#1e3a5f,#38bdf8)',
      'linear-gradient(135deg,#4a044e,#d946ef)',
    ];
    let hash = 0;
    for (let i = 0; i < (name?.length ?? 0); i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return palettes[Math.abs(hash) % palettes.length];
  }

  private emptyForm(): BrandForm {
    return { name: '', status: 'ACTIVE' };
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { show: true, message, type };
    setTimeout(() => this.toast.show = false, 3000);
  }
}