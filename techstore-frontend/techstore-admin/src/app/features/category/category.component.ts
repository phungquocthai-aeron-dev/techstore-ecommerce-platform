import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { CategoryService } from '../product/category.service';
import { CategoryCreateRequest, CategoryResponse, CategoryUpdateRequest } from '../product/models/category.model';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';

interface StatCard {
  label:     string;
  icon:      string;
  color:     string;
  bg:        string;
  count:     number;
  filterVal: string;
}

interface ToastState {
  show:    boolean;
  message: string;
  type:    'success' | 'error';
}

// Mutable form type
interface CategoryForm {
  name:              string;
  categoryType:      string;
  pcComponentType:   string;
  description:       string;
}

@Component({
  selector:    'app-category',
  templateUrl: './category.component.html',
  styleUrls:   ['./category.component.css'],
  standalone: true,
  imports: [FormsModule, NgIf, NgFor]
})
export class CategoryManagementComponent implements OnInit, OnDestroy {

  private destroy$      = new Subject<void>();
  private searchSubject = new Subject<string>();

  // ─── List ──────────────────────────────────────────────────────────────
  categories:    CategoryResponse[] = [];
  loading        = false;
  viewMode: 'table' | 'grid' = 'table';

  // ─── Filter / sort ─────────────────────────────────────────────────────
  searchKeyword      = '';
  filterCategoryType = '';
  filterType         = '';           // stat card filter (mirrors filterCategoryType)
  sortBy             = 'id';
  sortDirection      = 'DESC';

  // ─── Pagination ────────────────────────────────────────────────────────
  currentPage   = 0;
  pageSize      = 12;
  totalPages    = 0;
  totalElements = 0;
  pageNumbers:  number[] = [];

  // ─── Checkbox ──────────────────────────────────────────────────────────
  allChecked = false;
  checkedIds = new Set<number>();

  // ─── Stat cards ────────────────────────────────────────────────────────
  statCards: StatCard[] = [
    { label: 'Tất cả',     icon: 'bi-grid-fill',        color: '#2d7d32', bg: '#e8f5e9', count: 0, filterVal: '' },
    { label: 'Laptop',     icon: 'bi-laptop-fill',      color: '#1565c0', bg: '#e3f2fd', count: 0, filterVal: 'LAPTOP' },
    { label: 'Desktop',    icon: 'bi-pc-display-horizontal', color: '#283593', bg: '#e8eaf6', count: 0, filterVal: 'DESKTOP' },
    { label: 'Linh kiện',  icon: 'bi-cpu-fill',         color: '#e65100', bg: '#fff3e0', count: 0, filterVal: 'COMPONENT' },
    { label: 'Ngoại vi',   icon: 'bi-mouse-fill',       color: '#6a1b9a', bg: '#f3e5f5', count: 0, filterVal: 'PERIPHERAL' },
  ];

  // ─── Toast ─────────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };

  // ─── Form modal ────────────────────────────────────────────────────────
  showFormModal  = false;
  isEdit         = false;
  saving         = false;
  editingId: number | null = null;

  form: CategoryForm = this.emptyForm();

  get showPcComponentField(): boolean {
    return this.form.categoryType === 'COMPONENT';
  }

  // ─── Delete modal ──────────────────────────────────────────────────────
  showDeleteModal = false;
  deleteTarget:   CategoryResponse | null = null;

  constructor(private categoryService: CategoryService) {}

  // ══════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initSearch();
    this.loadCategories();
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
      this.loadCategories();
    });
  }

  loadCategories(): void {
    this.loading = true;

    const hasFilter = this.searchKeyword.trim() || this.filterCategoryType;

    const obs = hasFilter
      ? this.categoryService.search({
          keyword:      this.searchKeyword.trim() || undefined,
          categoryType: this.filterCategoryType || undefined,
          page:         this.currentPage,
          size:         this.pageSize,
          sortBy:       this.sortBy,
          sortDirection: this.sortDirection
        })
      : this.categoryService.getAll({
          page:          this.currentPage,
          size:          this.pageSize,
          sortBy:        this.sortBy,
          sortDirection: this.sortDirection
        });

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        const page = res.result;
        this.categories   = page?.content ?? [];
        this.totalElements = page?.totalElements ?? 0;
        this.totalPages   = page?.totalPages    ?? 0;
        this.buildPageNumbers();
        this.updateStatCounts();
        this.loading = false;
      },
      error: () => {
        this.showToast('Không thể tải danh mục', 'error');
        this.loading = false;
      }
    });
  }

  /** Derive counts from current full list (all no filter) */
  private updateStatCounts(): void {
    // Load full list once for counts
    this.categoryService.getAll({ page: 0, size: 999 })
      .pipe(takeUntil(this.destroy$))
      .subscribe(res => {
        const all = res.result?.content ?? [];
        this.statCards[0].count = all.length;
        this.statCards[1].count = all.filter(c => c.categoryType === 'LAPTOP').length;
        this.statCards[2].count = all.filter(c => c.categoryType === 'DESKTOP').length;
        this.statCards[3].count = all.filter(c => c.categoryType === 'COMPONENT').length;
        this.statCards[4].count = all.filter(c => c.categoryType === 'PERIPHERAL').length;
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // FILTERS
  // ══════════════════════════════════════════════════════════════════════

  onSearchChange(): void { this.searchSubject.next(this.searchKeyword); }

  clearSearch(): void {
    this.searchKeyword = '';
    this.currentPage   = 0;
    this.loadCategories();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadCategories();
  }

  filterByType(val: string): void {
    this.filterType         = val;
    this.filterCategoryType = val;
    this.currentPage        = 0;
    this.loadCategories();
  }

  // ══════════════════════════════════════════════════════════════════════
  // PAGINATION
  // ══════════════════════════════════════════════════════════════════════

  goPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.loadCategories();
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
    if (this.allChecked) this.categories.forEach(c => this.checkedIds.add(c.id));
    else this.categories.forEach(c => this.checkedIds.delete(c.id));
  }

  toggleCheck(id: number): void {
    if (this.checkedIds.has(id)) this.checkedIds.delete(id);
    else this.checkedIds.add(id);
    this.allChecked = this.categories.every(c => this.checkedIds.has(c.id));
  }

  // ══════════════════════════════════════════════════════════════════════
  // CREATE
  // ══════════════════════════════════════════════════════════════════════

  openCreateModal(): void {
    this.isEdit      = false;
    this.editingId   = null;
    this.form        = this.emptyForm();
    this.showFormModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════
  // EDIT
  // ══════════════════════════════════════════════════════════════════════

  editCategory(c: CategoryResponse): void {
    this.isEdit    = true;
    this.editingId = c.id;
    this.form = {
      name:            c.name,
      categoryType:    c.categoryType,
      pcComponentType: c.pcComponentType ?? '',
      description:     c.description    ?? ''
    };
    this.showFormModal = true;
  }

  closeFormModal(): void { this.showFormModal = false; }

  onCategoryTypeChange(): void {
    if (this.form.categoryType !== 'COMPONENT') {
      this.form.pcComponentType = '';
    }
  }

  saveCategory(): void {
    if (!this.form.name?.trim()) {
      this.showToast('Vui lòng nhập tên danh mục', 'error'); return;
    }
    if (!this.form.categoryType) {
      this.showToast('Vui lòng chọn loại danh mục', 'error'); return;
    }

    this.saving = true;

    const req: CategoryCreateRequest | CategoryUpdateRequest = {
      name:            this.form.name.trim(),
      categoryType:    this.form.categoryType,
      pcComponentType: this.form.pcComponentType || undefined,
      description:     this.form.description     || undefined
    };

    const obs = this.isEdit && this.editingId != null
      ? this.categoryService.update(this.editingId, req as CategoryUpdateRequest)
      : this.categoryService.create(req as CategoryCreateRequest);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving        = false;
        this.showFormModal = false;
        this.showToast(
          this.isEdit ? 'Cập nhật danh mục thành công!' : 'Thêm danh mục thành công!',
          'success'
        );
        this.loadCategories();
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Đã xảy ra lỗi', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════
  // DELETE
  // ══════════════════════════════════════════════════════════════════════

  confirmDelete(c: CategoryResponse): void {
    this.deleteTarget   = c;
    this.showDeleteModal = true;
  }

  executeDelete(): void {
    if (!this.deleteTarget) return;
    this.saving = true;

    this.categoryService.delete(this.deleteTarget.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving          = false;
          this.showDeleteModal = false;
          this.showToast('Đã xóa danh mục!', 'success');
          this.loadCategories();
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Không thể xóa danh mục', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════════════════

  getCategoryTypeLabel(type: string): string {
    const map: Record<string, string> = {
      LAPTOP:     'Laptop',
      DESKTOP:    'Desktop',
      COMPONENT:  'Linh kiện PC',
      PERIPHERAL: 'Ngoại vi',
      OTHER:      'Khác'
    };
    return map[type] ?? type;
  }

  getCatIcon(type: string): string {
    return {
      LAPTOP:     'bi-laptop-fill',
      DESKTOP:    'bi-pc-display-horizontal',
      COMPONENT:  'bi-cpu-fill',
      PERIPHERAL: 'bi-mouse-fill',
      OTHER:      'bi-tag-fill'
    }[type] ?? 'bi-tag-fill';
  }

  getCatBg(type: string): string {
    return {
      LAPTOP:     '#e3f2fd',
      DESKTOP:    '#e8eaf6',
      COMPONENT:  '#fff3e0',
      PERIPHERAL: '#f3e5f5',
      OTHER:      '#f5f5f5'
    }[type] ?? '#f5f5f5';
  }

  getCatColor(type: string): string {
    return {
      LAPTOP:     '#1565c0',
      DESKTOP:    '#283593',
      COMPONENT:  '#e65100',
      PERIPHERAL: '#6a1b9a',
      OTHER:      '#424242'
    }[type] ?? '#424242';
  }

  truncate(text: string, max: number): string {
    if (!text) return '';
    return text.length > max ? text.slice(0, max) + '…' : text;
  }

  private emptyForm(): CategoryForm {
    return { name: '', categoryType: '', pcComponentType: '', description: '' };
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { show: true, message, type };
    setTimeout(() => this.toast.show = false, 3000);
  }
}