import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { StaffService } from '../staff/staff.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;

  isLoading   = false;
  showPassword = false;
  isShaking   = false;

  alertMsg  = '';
  alertType: 'error' | 'success' = 'error';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private staffService: StaffService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username:      ['', [Validators.required, Validators.email]],
      password:   ['', Validators.required],
      rememberMe: [false]
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────

  isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  showAlert(msg: string, type: 'error' | 'success' = 'error'): void {
    this.alertMsg  = msg;
    this.alertType = type;
  }

  hideAlert(): void {
    this.alertMsg = '';
  }

  triggerShake(): void {
    this.isShaking = true;
    setTimeout(() => (this.isShaking = false), 400);
  }

  showForgot(event: Event): void {
    event.preventDefault();
    this.showAlert('Vui lòng liên hệ quản trị hệ thống để đặt lại mật khẩu: IT@company.vn');
  }

  // ─── Login ────────────────────────────────────────────────────────

  doLogin(): void {
    this.hideAlert();

    // Mark all fields touched to trigger validation display
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) return;

    this.isLoading = true;

    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: (res) => {
        this.isLoading = false;

        // roles từ StaffResponse có dạng "STAFF ADMIN" hoặc "STAFF"
        const roles: string = this.staffService.currentStaff?.roles ?? '';

        this.showAlert('Đăng nhập thành công! Đang chuyển hướng...', 'success');

        setTimeout(() => {
          this.router.navigate(['/']);
          // if (roles.includes('ADMIN')) {
          //   this.router.navigate(['/admin']);
          // } else {
          //   this.router.navigate(['/staff']);
          // }
        }, 900);
      },

      error: (err) => {
        this.isLoading = false;
        this.loginForm.get('password')?.reset();
        this.triggerShake();

        const code = err?.error?.code;

        if (code === 'INVALID_CREDENTIALS' || err?.status === 401) {
          this.showAlert('Sai username hoặc mật khẩu. Vui lòng thử lại.');
        } else if (err?.status === 423 || code === 'ACCOUNT_LOCKED') {
          this.showAlert('Tài khoản đã bị khoá. Liên hệ quản trị viên.');
        } else if (err?.status === 0) {
          this.showAlert('Không thể kết nối máy chủ. Kiểm tra lại mạng.');
        } else {
          this.showAlert('Đã có lỗi xảy ra. Vui lòng thử lại sau.');
        }
      }
    });
  }
}