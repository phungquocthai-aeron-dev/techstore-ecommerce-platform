// admin-notification.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';

import { AdminNotificationService } from './notification.service';
import { CustomerService } from '../customer/customer.service';
import { Customer } from '../customer/models/customer.model';
import { NotificationRequest, NotificationResponse } from './models/notification.model';


interface ToastState {
  show: boolean;
  message: string;
  type: 'success' | 'error';
}

interface FormState {
  title: string;
  content: string;
  targetMode: 'all' | 'specific';
  selectedCustomer: Customer | null;
  customerSearch: string;
}

@Component({
  selector: 'app-admin-notification',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class AdminNotificationComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();
  private customerSearchSubject = new Subject<string>();

  // ─── List state ──────────────────────────────────────────────────────
  notifications: NotificationResponse[] = [];
  loading = false;
  saving = false;
  deleting = false;

  // ─── Pagination ──────────────────────────────────────────────────────
  currentPage = 1;
  totalPages = 1;
  pageSize = 10;
  totalElements = 0;

  // ─── Filter ──────────────────────────────────────────────────────────
  searchKeyword = '';
  fromDate = '';
  toDate = '';
  activeFilter: 'all' | 'read' | 'unread' = 'all';
  isSearchMode = false;

  // ─── Stats (computed from current page) ──────────────────────────────
  get readCount()   { return this.notifications.filter(n => n.isRead).length; }
  get unreadCount() { return this.notifications.filter(n => !n.isRead).length; }

  // ─── Customer search ──────────────────────────────────────────────────
  customerSearchResults: Customer[] = [];
  customerSearchLoading = false;

  // ─── Modals ──────────────────────────────────────────────────────────
  showFormModal = false;
  showDetailModal = false;
  showDeleteModal = false;
  isEdit = false;

  editingId: string | null = null;
  detailTarget: NotificationResponse | null = null;
  deleteTarget: NotificationResponse | null = null;

  form: FormState = this.emptyForm();

  // ─── Toast ───────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };

  constructor(
    private notifService: AdminNotificationService,
    private customerService: CustomerService
  ) {}

  // ══════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initDebounce();
    this.loadAll();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 1;
      this.runSearch();
    });

    this.customerSearchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(kw => this.fetchCustomers(kw));
  }

  // ══════════════════════════════════════════════════════════════════════
  // DATA LOADING
  // ══════════════════════════════════════════════════════════════════════

  loadAll(): void {
    this.loading = true;
    this.isSearchMode = false;

    this.notifService.getAll(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.applyPage(res.result);
          this.loading = false;
        },
        error: () => {
          this.showToast('Không thể tải danh sách thông báo', 'error');
          this.loading = false;
        }
      });
  }

  private runSearch(): void {
    const hasFilter = this.searchKeyword.trim() || this.fromDate || this.toDate;

    if (!hasFilter) {
      this.isSearchMode = false;
      this.loadAll();
      return;
    }

    this.loading = true;
    this.isSearchMode = true;

    this.notifService.search({
      title:    this.searchKeyword.trim() || undefined,
      fromDate: this.fromDate || undefined,
      toDate:   this.toDate   || undefined,
      page:     this.currentPage,
      size:     this.pageSize
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.applyPage(res.result);
        this.loading = false;
      },
      error: () => {
        this.showToast('Tìm kiếm thất bại', 'error');
        this.loading = false;
      }
    });
  }

  private applyPage(page: any): void {
    if (!page) return;
    this.notifications = page.data ?? [];
    this.totalPages    = page.totalPages ?? 1;
    this.totalElements = page.totalElements ?? 0;
    this.currentPage   = page.currentPage ?? 1;
  }

  onSearchChange(): void { this.searchSubject.next(this.searchKeyword); }
  onDateChange(): void   { this.currentPage = 1; this.runSearch(); }
  applySearch(): void    { this.currentPage = 1; this.runSearch(); }

  clearSearch(): void {
    this.searchKeyword = '';
    this.fromDate = '';
    this.toDate = '';
    this.activeFilter = 'all';
    this.currentPage = 1;
    this.loadAll();
  }

  // ══════════════════════════════════════════════════════════════════════
  // FILTER (client-side on current page)
  // ══════════════════════════════════════════════════════════════════════

  filterBy(f: 'all' | 'read' | 'unread'): void { this.activeFilter = f; }

  get filteredNotifications(): NotificationResponse[] {
    if (this.activeFilter === 'read')   return this.notifications.filter(n => n.isRead);
    if (this.activeFilter === 'unread') return this.notifications.filter(n => !n.isRead);
    return this.notifications;
  }

  // ══════════════════════════════════════════════════════════════════════
  // PAGINATION
  // ══════════════════════════════════════════════════════════════════════

  goPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.currentPage = p;
    this.isSearchMode ? this.runSearch() : this.loadAll();
  }

  get pages(): number[] {
    const start = Math.max(1, this.currentPage - 2);
    const end   = Math.min(this.totalPages, this.currentPage + 2);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  // ══════════════════════════════════════════════════════════════════════
  // CUSTOMER SEARCH
  // ══════════════════════════════════════════════════════════════════════

  onCustomerSearchChange(): void {
    this.form.selectedCustomer = null;
    this.customerSearchSubject.next(this.form.customerSearch);
  }

  private fetchCustomers(kw: string): void {
    if (!kw.trim()) {
      this.customerSearchResults = [];
      return;
    }

    this.customerSearchLoading = true;

    const isNumeric = /^\d+$/.test(kw.trim());
    const params = isNumeric
      ? { phone: kw.trim() }
      : kw.includes('@')
        ? { email: kw.trim() }
        : { fullName: kw.trim() };

    this.customerService.search(params)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.customerSearchResults = res.result ?? [];
          this.customerSearchLoading = false;
        },
        error: () => { this.customerSearchLoading = false; }
      });
  }

  selectCustomer(c: Customer): void {
    this.form.selectedCustomer = c;
    this.form.customerSearch = `${c.fullName} (${c.email})`;
    this.customerSearchResults = [];
  }

  clearCustomerSelection(): void {
    this.form.selectedCustomer = null;
    this.form.customerSearch = '';
    this.customerSearchResults = [];
  }

  onTargetModeChange(): void {
    if (this.form.targetMode === 'all') {
      this.clearCustomerSelection();
    }
  }

  // ══════════════════════════════════════════════════════════════════════
  // CREATE / EDIT
  // ══════════════════════════════════════════════════════════════════════

  openCreateModal(): void {
    this.isEdit = false;
    this.editingId = null;
    this.form = this.emptyForm();
    this.customerSearchResults = [];
    this.showFormModal = true;
  }

  openEditModal(n: NotificationResponse): void {
    this.isEdit = true;
    this.editingId = n.id;
    const isBcast = this.isBroadcast(n);
    this.form = {
      title:            n.title,
      content:          n.content,
      targetMode:       isBcast ? 'all' : 'specific',
      selectedCustomer: null,
      customerSearch:   isBcast ? '' : `User #${n.userId}`
    };
    this.customerSearchResults = [];
    this.showFormModal = true;
  }

  closeFormModal(): void {
    this.showFormModal = false;
    this.customerSearchResults = [];
  }

  savePost(): void {
    if (!this.form.title?.trim()) {
      this.showToast('Vui lòng nhập tiêu đề thông báo', 'error'); return;
    }
    if (!this.form.content?.trim()) {
      this.showToast('Vui lòng nhập nội dung thông báo', 'error'); return;
    }
    if (!this.isEdit && this.form.targetMode === 'specific' && !this.form.selectedCustomer) {
      this.showToast('Vui lòng chọn người nhận cụ thể', 'error'); return;
    }

    this.saving = true;

    const req: NotificationRequest = {
      title:   this.form.title.trim(),
      content: this.form.content.trim(),
      userId:  this.form.targetMode === 'all' ? 0 : (this.form.selectedCustomer?.id ?? 0)
    };

    const obs = this.isEdit && this.editingId
      ? this.notifService.update(this.editingId, req)
      : this.notifService.create(req);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving = false;
        this.showFormModal = false;
        this.showToast(
          this.isEdit ? 'Cập nhật thông báo thành công!' : 'Tạo thông báo thành công!',
          'success'
        );
        this.currentPage = 1;
        this.loadAll();
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

  openDeleteModal(n: NotificationResponse): void {
    this.deleteTarget = n;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.deleting = true;

    this.notifService.delete(this.deleteTarget.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.deleting = false;
          this.showDeleteModal = false;
          this.showToast('Đã xoá thông báo', 'success');
          if (this.notifications.length === 1 && this.currentPage > 1) this.currentPage--;
          this.isSearchMode ? this.runSearch() : this.loadAll();
        },
        error: err => {
          this.deleting = false;
          this.showToast(err?.error?.message ?? 'Xoá thất bại', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // DETAIL
  // ══════════════════════════════════════════════════════════════════════

  openDetail(n: NotificationResponse): void {
    this.detailTarget = n;
    this.showDetailModal = true;
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.detailTarget = null;
  }

  // ══════════════════════════════════════════════════════════════════════
  // MARK AS READ
  // ══════════════════════════════════════════════════════════════════════

  markAsRead(n: NotificationResponse): void {
    if (n.isRead) return;
    this.notifService.markAsRead(n.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => { n.isRead = true; this.showToast('Đã đánh dấu đã đọc', 'success'); },
        error: () => this.showToast('Không thể cập nhật trạng thái', 'error')
      });
  }

  markAllAsRead(): void {
    this.notifService.markAllAsRead()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notifications.forEach(n => n.isRead = true);
          this.showToast('Đã đánh dấu tất cả là đã đọc', 'success');
        },
        error: () => this.showToast('Không thể cập nhật', 'error')
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // HELPERS
  // ══════════════════════════════════════════════════════════════════════

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  formatRelativeDate(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const min  = Math.floor(diff / 60000);
    const hour = Math.floor(min / 60);
    const day  = Math.floor(hour / 24);
    if (min < 1)   return 'Vừa xong';
    if (min < 60)  return `${min} phút trước`;
    if (hour < 24) return `${hour} giờ trước`;
    if (day < 7)   return `${day} ngày trước`;
    return this.formatDate(dateStr);
  }

  truncate(text: string, max = 75): string {
    return text?.length > max ? text.slice(0, max) + '…' : (text ?? '');
  }

  isBroadcast(n: NotificationResponse): boolean {
    return !n.userId || n.userId === '0';
  }

  private emptyForm(): FormState {
    return { title: '', content: '', targetMode: 'all', selectedCustomer: null, customerSearch: '' };
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { show: true, message, type };
    setTimeout(() => this.toast.show = false, 3200);
  }

  get isAllMode(): boolean {
    return this.form.targetMode === 'all';
  }
}