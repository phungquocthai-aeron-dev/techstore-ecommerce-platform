import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { StaffService } from './staff.service';
import {
  StaffResponse,
  StaffRequest,
  StaffRoleUpdateRequest
} from './models/staff.model';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';

interface StaffForm {
  fullName:  string;
  email:     string;
  phone:     string;
  password:  string;
  status:    string;
  roleNames: string[];
}

interface PwdForm {
  oldPassword:     string;
  newPassword:     string;
  passwordConfirm: string;
}

interface ToastState {
  show:    boolean;
  message: string;
  type:    'success' | 'error';
}

@Component({
  selector:    'app-staff',
  templateUrl: './staff.component.html',
  styleUrls:   ['./staff.component.css'],
  standalone: true,
  imports: [FormsModule, NgIf, NgFor]
})
export class StaffManagementComponent implements OnInit, OnDestroy {

  private destroy$      = new Subject<void>();
  private searchSubject = new Subject<string>();

  // ─── List ──────────────────────────────────────────────────────────────
  allStaff:   StaffResponse[] = [];   // full list (from search/getAll)
  staffList:  StaffResponse[] = [];   // filtered for display
  loading     = false;

  // ─── Filter ────────────────────────────────────────────────────────────
  searchKeyword    = '';
  activeStatFilter = 'all';

  // ─── Counts ────────────────────────────────────────────────────────────
  totalCount    = 0;
  activeCount   = 0;
  inactiveCount = 0;
  adminCount    = 0;

  // ─── Lookup ────────────────────────────────────────────────────────────
  availableRoles = [
    { value: 'STAFF', label: 'Nhân viên', desc: 'Truy cập các tính năng quản lý thông thường' },
    { value: 'ADMIN', label: 'Quản trị viên', desc: 'Toàn quyền quản trị hệ thống' }
  ];

  statusOptions = [
    { value: 'ACTIVE',   label: 'Đang hoạt động' },
    { value: 'INACTIVE', label: 'Tạm ngừng'       }
  ];

  // ─── Toast ─────────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };
  saving = false;

  // ─── Form modal (create / edit / view) ─────────────────────────────────
  showFormModal = false;
  isEdit        = false;
  isView        = false;
  editingId: number | null = null;
  viewTarget:   StaffResponse | null = null;
  showPwd       = false;

  staffForm: StaffForm = this.emptyStaffForm();

  // ─── Role modal ────────────────────────────────────────────────────────
  showRoleModal  = false;
  roleTarget:    StaffResponse | null = null;
  selectedRoles: string[] = [];

  // ─── Password modal ────────────────────────────────────────────────────
  showPwdModal   = false;
  pwdTarget:     StaffResponse | null = null;
  showOldPwd     = false;
  showNewPwd     = false;
  showConfirmPwd = false;
  pwdForm: PwdForm = this.emptyPwdForm();

  // ─── Status modal ──────────────────────────────────────────────────────
  showStatusModal = false;
  statusTarget:   StaffResponse | null = null;

  constructor(private staffService: StaffService) {}

  // ══════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initSearchDebounce();
    this.loadAll();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ══════════════════════════════════════════════════════════════════════
  // DATA LOADING
  // ══════════════════════════════════════════════════════════════════════

  private initSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(kw => this.applyFilters(kw));
  }

  /** Load all staff (search with no params returns everyone) */
  loadAll(): void {
    this.loading = true;
    this.staffService.search({})
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.allStaff = res.result ?? [];
          this.calcCounts();
          this.applyFilters(this.searchKeyword);
          this.loading = false;
        },
        error: () => {
          this.showToast('Không thể tải danh sách nhân viên', 'error');
          this.loading = false;
        }
      });
  }

  private calcCounts(): void {
    this.totalCount    = this.allStaff.length;
    this.activeCount   = this.allStaff.filter(s => s.status === 'ACTIVE').length;
    this.inactiveCount = this.allStaff.filter(s => s.status === 'INACTIVE').length;
    this.adminCount    = this.allStaff.filter(s => this.parseRoles(s.roles).includes('ADMIN')).length;
  }

  private applyFilters(kw: string): void {
    let list = [...this.allStaff];

    // Keyword filter
    if (kw.trim()) {
      const k = kw.toLowerCase();
      list = list.filter(s =>
        s.fullName?.toLowerCase().includes(k) ||
        s.email?.toLowerCase().includes(k) ||
        s.phone?.includes(k) ||
        String(s.id).includes(k)
      );
    }

    // Stat filter
    if (this.activeStatFilter === 'ACTIVE' || this.activeStatFilter === 'INACTIVE') {
      list = list.filter(s => s.status === this.activeStatFilter);
    } else if (this.activeStatFilter === 'ADMIN') {
      list = list.filter(s => this.parseRoles(s.roles).includes('ADMIN'));
    }

    this.staffList = list;
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

  // ══════════════════════════════════════════════════════════════════════
  // VIEW
  // ══════════════════════════════════════════════════════════════════════

  viewStaff(s: StaffResponse): void {
    this.viewTarget   = s;
    this.isView       = true;
    this.isEdit       = false;
    this.showFormModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════
  // CREATE
  // ══════════════════════════════════════════════════════════════════════

  openCreateModal(): void {
    this.isView       = false;
    this.isEdit       = false;
    this.editingId    = null;
    this.staffForm    = this.emptyStaffForm();
    this.showPwd      = false;
    this.showFormModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════
  // EDIT
  // ══════════════════════════════════════════════════════════════════════

  editStaff(s: StaffResponse): void {
    this.isView    = false;
    this.isEdit    = true;
    this.editingId = s.id;
    this.staffForm = {
      fullName:  s.fullName,
      email:     s.email,
      phone:     s.phone ?? '',
      password:  '',
      status:    s.status,
      roleNames: this.parseRoles(s.roles)
    };
    this.showFormModal = true;
  }

  closeFormModal(): void { this.showFormModal = false; }

  saveStaff(): void {
    if (!this.staffForm.fullName?.trim()) {
      this.showToast('Vui lòng nhập họ và tên', 'error'); return;
    }
    if (!this.staffForm.email?.trim()) {
      this.showToast('Vui lòng nhập email', 'error'); return;
    }
    if (!this.isEdit && !this.staffForm.password) {
      this.showToast('Vui lòng nhập mật khẩu', 'error'); return;
    }
    if (!this.isEdit && this.staffForm.roleNames.length === 0) {
      this.showToast('Vui lòng chọn ít nhất một vai trò', 'error'); return;
    }

    this.saving = true;

    const req: StaffRequest = {
      fullName:  this.staffForm.fullName.trim(),
      email:     this.staffForm.email.trim(),
      phone:     this.staffForm.phone   || undefined,
      status:    this.isEdit ? this.staffForm.status : undefined,
      roleNames: !this.isEdit ? this.staffForm.roleNames : undefined
    };

    const obs = this.isEdit && this.editingId != null
      ? this.staffService.updateInfo(this.editingId, req)
      : this.staffService.create(req);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving        = false;
        this.showFormModal = false;
        this.showToast(
          this.isEdit ? 'Cập nhật nhân viên thành công!' : 'Thêm nhân viên thành công!',
          'success'
        );
        this.loadAll();
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Đã xảy ra lỗi', 'error');
      }
    });
  }

  toggleRole(role: string): void {
    const idx = this.staffForm.roleNames.indexOf(role);
    if (idx >= 0) this.staffForm.roleNames.splice(idx, 1);
    else this.staffForm.roleNames.push(role);
  }

  // ══════════════════════════════════════════════════════════════════════
  // ROLES
  // ══════════════════════════════════════════════════════════════════════

  openRoleModal(s: StaffResponse): void {
    this.roleTarget    = s;
    this.selectedRoles = [...this.parseRoles(s.roles)];
    this.showRoleModal = true;
  }

  toggleSelectedRole(role: string): void {
    const idx = this.selectedRoles.indexOf(role);
    if (idx >= 0) this.selectedRoles.splice(idx, 1);
    else this.selectedRoles.push(role);
  }

  saveRoles(): void {
    if (!this.roleTarget) return;
    this.saving = true;

    const req: StaffRoleUpdateRequest = { roleNames: this.selectedRoles };

    this.staffService.updateRoles(this.roleTarget.id, req)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.saving        = false;
          this.showRoleModal = false;
          // Update local
          const staff = this.allStaff.find(s => s.id === this.roleTarget!.id);
          if (staff && res.result) { Object.assign(staff, res.result); }
          this.calcCounts();
          this.applyFilters(this.searchKeyword);
          this.showToast('Cập nhật quyền thành công!', 'success');
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Lỗi cập nhật quyền', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════
  // PASSWORD
  // ══════════════════════════════════════════════════════════════════════

  openPasswordModal(s: StaffResponse): void {
    this.pwdTarget     = s;
    this.pwdForm       = this.emptyPwdForm();
    this.showOldPwd    = false;
    this.showNewPwd    = false;
    this.showConfirmPwd = false;
    this.showPwdModal  = true;
  }

  canSavePwd(): boolean {
    return !!(
      this.pwdForm.oldPassword &&
      this.pwdForm.newPassword &&
      this.pwdForm.passwordConfirm &&
      this.pwdForm.newPassword === this.pwdForm.passwordConfirm
    );
  }

  savePassword(): void {
    if (!this.pwdTarget || !this.canSavePwd()) return;
    this.saving = true;

    this.staffService.updatePassword(
      this.pwdTarget.id,
      this.pwdForm.oldPassword,
      this.pwdForm.newPassword,
      this.pwdForm.passwordConfirm
    ).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving       = false;
        this.showPwdModal = false;
        this.showToast('Đổi mật khẩu thành công!', 'success');
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Mật khẩu không đúng hoặc lỗi hệ thống', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════
  // STATUS
  // ══════════════════════════════════════════════════════════════════════

  openStatusModal(s: StaffResponse): void {
    this.statusTarget   = s;
    this.showStatusModal = true;
  }

  executeToggleStatus(): void {
    if (!this.statusTarget) return;
    this.saving = true;
    const newStatus = this.nextStatus(this.statusTarget.status);

    this.staffService.updateStatus(this.statusTarget.id, newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving           = false;
          this.showStatusModal  = false;
          const staff = this.allStaff.find(s => s.id === this.statusTarget!.id);
          if (staff) staff.status = newStatus;
          this.calcCounts();
          this.applyFilters(this.searchKeyword);
          this.showToast('Cập nhật trạng thái thành công!', 'success');
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

  /** Parse roles string "ROLE_STAFF,ROLE_ADMIN" or "STAFF" → ['STAFF', 'ADMIN'] */
  parseRoles(roles: string): string[] {
    if (!roles) return [];
    return roles.split(',')
      .map(r => r.trim().replace(/^ROLE_/, ''))
      .filter(Boolean);
  }

  getStatusLabel(status: string): string {
    return { ACTIVE: 'Đang hoạt động', INACTIVE: 'Tạm ngừng', DISABLED: 'Vô hiệu' }[status] ?? status;
  }

  nextStatus(status: string): string {
    return status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
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
      'linear-gradient(135deg,#1d4ed8,#60a5fa)',
      'linear-gradient(135deg,#7c3aed,#a78bfa)',
      'linear-gradient(135deg,#0f766e,#34d399)',
      'linear-gradient(135deg,#b45309,#fbbf24)',
      'linear-gradient(135deg,#be185d,#f472b6)',
      'linear-gradient(135deg,#0e7490,#38bdf8)',
      'linear-gradient(135deg,#166534,#86efac)',
    ];
    let h = 0;
    for (let i = 0; i < (name?.length ?? 0); i++) h = name.charCodeAt(i) + ((h << 5) - h);
    return colors[Math.abs(h) % colors.length];
  }

  private emptyStaffForm(): StaffForm {
    return { fullName: '', email: '', phone: '', password: '', status: 'ACTIVE', roleNames: ['STAFF'] };
  }

  private emptyPwdForm(): PwdForm {
    return { oldPassword: '', newPassword: '', passwordConfirm: '' };
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { show: true, message, type };
    setTimeout(() => this.toast.show = false, 3000);
  }
}