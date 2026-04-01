import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CouponService } from './coupon.service';
import { CouponResponse } from './models/coupon.model';
import { CouponRequest } from './models/coupon-request.model';

const EMPTY_FORM = (): CouponRequest => ({
  name:          '',
  discountType:  'PERCENTAGE',
  discountValue: 0,
  minOrderValue: 0,
  maxDiscount:   0,
  startDate:     '',
  endDate:       '',
  usageLimit:    1,
  couponType: 'PUBLIC',
});

@Component({
  selector: 'app-coupon',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './coupon.component.html',
  styleUrls: ['./coupon.component.css']
})
export class CouponComponent implements OnInit {

  coupons:  CouponResponse[] = [];
  filtered: CouponResponse[] = [];

  isLoading = false;
  isSaving  = false;

  alertMsg  = '';
  alertType: 'success' | 'error' = 'success';

  // toolbar
  searchKeyword = '';
  filterType    = '';
  filterStatus  = '';

  // modal create/edit
  showModal  = false;
  editingId: number | null = null;
  form: CouponRequest = EMPTY_FORM();

  // modal delete
  showDeleteModal  = false;
  deletingCoupon: CouponResponse | null = null;

  // ─── Computed stats ───────────────────────────────────────────────

  get activeCount(): number {
    return this.coupons.filter(c => c.status === 'ACTIVE').length;
  }

  get pendingCount(): number {
    return this.coupons.filter(c => new Date(c.startDate) > new Date()).length;
  }

  get expiredCount(): number {
    return this.coupons.filter(c => c.status === 'EXPIRED' || c.status === 'INACTIVE').length;
  }

  constructor(private couponService: CouponService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  // ─── Load ─────────────────────────────────────────────────────────

  loadAll(): void {
    this.isLoading = true;
    this.couponService.getAll().subscribe({
      next: res => {
        this.coupons  = res.result ?? [];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.showAlert('Không thể tải danh sách mã giảm giá.', 'error');
        this.isLoading = false;
      }
    });
  }

  // ─── Filter ───────────────────────────────────────────────────────

  applyFilter(): void {
    let result = [...this.coupons];

    if (this.searchKeyword.trim()) {
      const q = this.searchKeyword.toLowerCase();
      result = result.filter(c => c.name.toLowerCase().includes(q));
    }

    if (this.filterType) {
      result = result.filter(c => c.discountType === this.filterType);
    }

    if (this.filterStatus) {
      result = result.filter(c => c.status === this.filterStatus);
    }

    this.filtered = result;
  }

  // ─── Modal ────────────────────────────────────────────────────────

  openCreateModal(): void {
    this.editingId = null;
    this.form = EMPTY_FORM();
    this.showModal = true;
  }

  openEditModal(c: CouponResponse): void {
    this.editingId = c.id;
    this.form = {
      name:          c.name,
      discountType:  c.discountType,
      discountValue: c.discountValue,
      minOrderValue: c.minOrderValue,
      maxDiscount:   c.maxDiscount,
      startDate:     this.toInputDateTime(c.startDate),
      endDate:       this.toInputDateTime(c.endDate),
      usageLimit:    c.usageLimit,
      couponType: 'PUBLIC'
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  // ─── Save ─────────────────────────────────────────────────────────

  saveCoupon(): void {
    if (!this.form.name.trim()) {
      this.showAlert('Vui lòng nhập tên mã giảm giá.', 'error'); return;
    }
    if (!this.form.startDate || !this.form.endDate) {
      this.showAlert('Vui lòng chọn thời gian hiệu lực.', 'error'); return;
    }
    if (new Date(this.form.startDate) >= new Date(this.form.endDate)) {
      this.showAlert('Ngày kết thúc phải sau ngày bắt đầu.', 'error'); return;
    }

    this.isSaving = true;

    // Convert datetime-local → ISO string for backend
    const payload: CouponRequest = {
      ...this.form,
      startDate: new Date(this.form.startDate).toISOString(),
      endDate:   new Date(this.form.endDate).toISOString(),
    };

    const obs = this.editingId
      ? this.couponService.updateCoupon(this.editingId, payload)
      : this.couponService.createCoupon(payload);

    obs.subscribe({
      next: () => {
        this.isSaving = false;
        this.closeModal();
        this.loadAll();
        this.showAlert(this.editingId ? 'Cập nhật mã thành công.' : 'Tạo mã giảm giá thành công.');
      },
      error: () => {
        this.isSaving = false;
        this.showAlert('Lưu thất bại. Vui lòng thử lại.', 'error');
      }
    });
  }

  // ─── Delete ───────────────────────────────────────────────────────

  confirmDelete(c: CouponResponse): void {
    this.deletingCoupon  = c;
    this.showDeleteModal = true;
  }

  deleteCoupon(): void {
    if (!this.deletingCoupon) return;
    this.isSaving = true;
    this.couponService.deleteCoupon(this.deletingCoupon.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.showDeleteModal = false;
        this.coupons  = this.coupons.filter(c => c.id !== this.deletingCoupon!.id);
        this.applyFilter();
        this.showAlert('Đã xoá mã giảm giá.');
      },
      error: () => {
        this.isSaving = false;
        this.showAlert('Xoá thất bại.', 'error');
      }
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────

  getUsagePct(c: CouponResponse): number {
    if (!c.usageLimit) return 0;
    return Math.min(100, Math.round((c.usedCount / c.usageLimit) * 100));
  }

  isExpired(dateStr: string): boolean {
    return new Date(dateStr) < new Date();
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      ACTIVE:   'Hoạt động',
      INACTIVE: 'Vô hiệu',
      EXPIRED:  'Hết hạn',
    };
    return map[status] ?? status;
  }

  /** Convert ISO/backend date → datetime-local input format */
  toInputDateTime(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  /** Used in template for preview */
  toDate(str: string): Date {
    return new Date(str);
  }

  // ─── Alert ────────────────────────────────────────────────────────

  showAlert(msg: string, type: 'success' | 'error' = 'success'): void {
    this.alertMsg  = msg;
    this.alertType = type;
    setTimeout(() => (this.alertMsg = ''), 3500);
  }
}