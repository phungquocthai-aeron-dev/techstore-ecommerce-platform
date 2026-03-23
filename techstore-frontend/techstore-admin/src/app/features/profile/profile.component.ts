import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StaffService } from '../staff/staff.service';
import { StaffResponse, StaffRequest } from '../staff/models/staff.model';
import { PermissionService } from '../../core/services/permission.service';

interface Toast { show: boolean; type: 'success' | 'error'; message: string; }

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  staff: StaffResponse | null = null;
  activeTab: 'info' | 'password' = 'info';
  saving = false;
  Math = Math;

  // ── Info form ─────────────────────────────────
  infoForm: StaffRequest = { fullName: '', phone: '' };
  infoEditing = false;

  // ── Password form ─────────────────────────────
  pwdForm = { oldPassword: '', newPassword: '', passwordConfirm: '' };
  showOld = false;
  showNew = false;
  showConfirm = false;

  toast: Toast = { show: false, type: 'success', message: '' };

  constructor(
    public staffService: StaffService,
    public perm: PermissionService
  ) {}

  ngOnInit(): void {
    this.staffService.currentStaff$.subscribe(s => {
      this.staff = s;
      if (s) {
        this.infoForm = { fullName: s.fullName, phone: s.phone };
      }
    });
  }

  // ── Helpers ───────────────────────────────────

  getAvatarText(name: string): string {
    if (!name) return 'ST';
    const parts = name.trim().split(' ');
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  cancelEdit(): void {
    if (this.staff) {
      this.infoForm = { fullName: this.staff.fullName, phone: this.staff.phone };
    }
    this.infoEditing = false;
  }

  get pwdMismatch(): boolean {
    return !!(this.pwdForm.newPassword && this.pwdForm.passwordConfirm
      && this.pwdForm.newPassword !== this.pwdForm.passwordConfirm);
  }

  get pwdCanSave(): boolean {
    return !!(this.pwdForm.oldPassword && this.pwdForm.newPassword
      && this.pwdForm.passwordConfirm
      && !this.pwdMismatch
      && this.pwdForm.newPassword.length >= 6);
  }

  // ── Save info ─────────────────────────────────

  saveInfo(): void {
    if (!this.staff || this.saving) return;
    this.saving = true;

    this.staffService.updateInfo(this.staff.id, this.infoForm).subscribe({
      next: () => {
        this.saving = false;
        this.infoEditing = false;
        this.showToast('success', 'Cập nhật thông tin thành công!');
      },
      error: () => {
        this.saving = false;
        this.showToast('error', 'Cập nhật thất bại, vui lòng thử lại.');
      }
    });
  }

  // ── Save password ─────────────────────────────

  savePassword(): void {
    if (!this.staff || !this.pwdCanSave || this.saving) return;
    this.saving = true;

    this.staffService.updatePassword(
      this.staff.id,
      this.pwdForm.oldPassword,
      this.pwdForm.newPassword,
      this.pwdForm.passwordConfirm
    ).subscribe({
      next: () => {
        this.saving = false;
        this.pwdForm = { oldPassword: '', newPassword: '', passwordConfirm: '' };
        this.showToast('success', 'Đổi mật khẩu thành công!');
      },
      error: () => {
        this.saving = false;
        this.showToast('error', 'Mật khẩu hiện tại không đúng hoặc có lỗi xảy ra.');
      }
    });
  }

  // ── Toast ─────────────────────────────────────

  private showToast(type: 'success' | 'error', message: string): void {
    this.toast = { show: true, type, message };
    setTimeout(() => this.toast.show = false, 3200);
  }
}