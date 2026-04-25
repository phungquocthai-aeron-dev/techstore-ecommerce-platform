// forgot-password.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ForgotPasswordService } from './forgot-password.service';

type Step = 'email' | 'otp' | 'reset' | 'success';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  step: Step = 'email';

  // Step 1
  email = '';

  // Step 2
  otp = '';
  otpDigits: string[] = ['', '', '', '', '', ''];

  // Step 3
  newPassword = '';
  passwordConfirm = '';
  showPassword = false;
  showConfirm = false;

  loading = false;
  errorMsg = '';
  successMsg = '';

  // Countdown resend OTP
  resendCountdown = 0;
  private countdownInterval: any;

  constructor(
    private forgotPasswordService: ForgotPasswordService,
    private router: Router
  ) {}

  // ─── Step 1: Gửi OTP ─────────────────────────────────────────────

  submitEmail(): void {
    if (!this.email.trim()) {
      this.errorMsg = 'Vui lòng nhập email.';
      return;
    }
    if (!this.isValidEmail(this.email)) {
      this.errorMsg = 'Email không hợp lệ.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';

    this.forgotPasswordService.sendOtp(this.email.trim()).subscribe({
      next: (res) => {
        this.loading = false;
        this.step = 'otp';
        this.startResendCountdown();
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Email không tồn tại trong hệ thống.';
      }
    });
  }

  // ─── Step 2: Xác nhận OTP ─────────────────────────────────────────

  onOtpInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/\D/g, '');
    this.otpDigits[index] = val ? val[val.length - 1] : '';
    input.value = this.otpDigits[index];

    if (val && index < 5) {
      const next = document.getElementById(`otp-${index + 1}`);
      next?.focus();
    }

    if (this.otpDigits.every(d => d !== '')) {
      this.otp = this.otpDigits.join('');
    }
  }

  onOtpKeydown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace' && !this.otpDigits[index] && index > 0) {
      const prev = document.getElementById(`otp-${index - 1}`);
      prev?.focus();
    }
  }

  onOtpPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const text = event.clipboardData?.getData('text') ?? '';
    const digits = text.replace(/\D/g, '').slice(0, 6).split('');
    digits.forEach((d, i) => {
      if (i < 6) this.otpDigits[i] = d;
    });
    this.otp = this.otpDigits.join('');

    // Focus last filled
    const lastIndex = Math.min(digits.length - 1, 5);
    const el = document.getElementById(`otp-${lastIndex}`);
    el?.focus();
  }

  submitOtp(): void {
    this.otp = this.otpDigits.join('');
    if (this.otp.length < 6) {
      this.errorMsg = 'Vui lòng nhập đầy đủ 6 chữ số OTP.';
      return;
    }
    this.errorMsg = '';
    this.step = 'reset';
  }

  resendOtp(): void {
    if (this.resendCountdown > 0) return;
    this.loading = true;
    this.errorMsg = '';
    this.otpDigits = ['', '', '', '', '', ''];
    this.otp = '';

    this.forgotPasswordService.sendOtp(this.email.trim()).subscribe({
      next: () => {
        this.loading = false;
        this.successMsg = 'OTP mới đã được gửi!';
        setTimeout(() => this.successMsg = '', 3000);
        this.startResendCountdown();
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Không thể gửi lại OTP. Thử lại sau.';
      }
    });
  }

  private startResendCountdown(seconds = 60): void {
    this.resendCountdown = seconds;
    clearInterval(this.countdownInterval);
    this.countdownInterval = setInterval(() => {
      this.resendCountdown--;
      if (this.resendCountdown <= 0) {
        clearInterval(this.countdownInterval);
      }
    }, 1000);
  }

  // ─── Step 3: Đặt mật khẩu mới ────────────────────────────────────

  submitReset(): void {
    this.errorMsg = '';

    if (!this.newPassword || this.newPassword.length < 6) {
      this.errorMsg = 'Mật khẩu phải có ít nhất 6 ký tự.';
      return;
    }
    if (this.newPassword !== this.passwordConfirm) {
      this.errorMsg = 'Mật khẩu xác nhận không khớp.';
      return;
    }

    this.loading = true;

    this.forgotPasswordService.resetPassword({
      email: this.email.trim(),
      otp: this.otp,
      newPassword: this.newPassword,
      passwordConfirm: this.passwordConfirm
    }).subscribe({
      next: () => {
        this.loading = false;
        this.step = 'success';
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'OTP không hợp lệ hoặc đã hết hạn.';
      }
    });
  }

  // ─── Navigation ───────────────────────────────────────────────────

  goBack(): void {
    if (this.step === 'otp') {
      this.step = 'email';
      this.errorMsg = '';
    } else if (this.step === 'reset') {
      this.step = 'otp';
      this.errorMsg = '';
    }
  }

  goLogin(): void {
    this.router.navigate(['/auth'], { queryParams: { tab: 'login' } });
  }

  // ─── Utils ────────────────────────────────────────────────────────

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  get passwordStrength(): { level: number; label: string; color: string } {
    const pw = this.newPassword;
    if (!pw) return { level: 0, label: '', color: '' };
    let score = 0;
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;

    if (score <= 1) return { level: 1, label: 'Yếu', color: '#ef4444' };
    if (score === 2) return { level: 2, label: 'Trung bình', color: '#f59e0b' };
    if (score === 3) return { level: 3, label: 'Mạnh', color: '#22c55e' };
    return { level: 4, label: 'Rất mạnh', color: '#16a34a' };
  }
}