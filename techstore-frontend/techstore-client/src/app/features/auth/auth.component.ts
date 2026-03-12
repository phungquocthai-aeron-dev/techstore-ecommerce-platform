import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CustomerRegisterRequest } from '../../features/customer/models/customer-register.model';
import { CustomerService } from '../user/customer.service';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class LoginComponent {
  activeTab: 'login' | 'register' = 'login';

  loginData = { identifier: '', password: '' };

  registerData: CustomerRegisterRequest & { passwordConfirm: string } = {
    email: '',
    password: '',
    passwordConfirm: '',
    fullName: '',
    phone: '',
    dob: ''
  };

  showLoginPassword = false;
  showRegisterPassword = false;
  showConfirmPassword = false;

  loginAlert: { message: string; type: 'success' | 'error' } | null = null;
  registerAlert: { message: string; type: 'success' | 'error' } | null = null;

  isLoginLoading = false;
  isRegisterLoading = false;

constructor(
  private authService: AuthService,
  private router: Router,
  private tokenService: TokenService,
  private customerService: CustomerService
) {}
  switchTab(tab: 'login' | 'register') {
    this.activeTab = tab;
    this.loginAlert = null;
    this.registerAlert = null;
  }

  togglePassword(field: 'login' | 'register' | 'confirm') {
    if (field === 'login') this.showLoginPassword = !this.showLoginPassword;
    else if (field === 'register') this.showRegisterPassword = !this.showRegisterPassword;
    else this.showConfirmPassword = !this.showConfirmPassword;
  }

  private showAlert(
    target: 'login' | 'register',
    message: string,
    type: 'success' | 'error',
    duration = 4000
  ) {
    if (target === 'login') {
      this.loginAlert = { message, type };
      setTimeout(() => (this.loginAlert = null), duration);
    } else {
      this.registerAlert = { message, type };
      setTimeout(() => (this.registerAlert = null), duration);
    }
  }

  handleLogin(form: NgForm) {
  if (form.invalid) {
    form.form.markAllAsTouched();
    return;
  }
  this.isLoginLoading = true;

  this.authService
    .loginCustomer({ username: this.loginData.identifier, password: this.loginData.password })
    .subscribe({
      next: () => {
        // Decode token lấy customer ID rồi load user vào state
        const token = this.tokenService.getToken()!;
        const payload = JSON.parse(atob(token.split('.')[1]));
        const customerId = Number(payload.sub); // sub = "1" → 1

        this.customerService.loadCurrentUser(customerId).subscribe({
          next: () => {
            this.isLoginLoading = false;
            this.showAlert('login', 'Đăng nhập thành công! Đang chuyển hướng...', 'success');
            setTimeout(() => this.router.navigate(['/home']), 1500);
          },
          error: () => {
            // Vẫn navigate dù load user lỗi
            this.isLoginLoading = false;
            this.showAlert('login', 'Đăng nhập thành công! Đang chuyển hướng...', 'success');
            setTimeout(() => this.router.navigate(['/home']), 1500);
          }
        });
      },
      error: (err) => {
        this.isLoginLoading = false;
        const msg = err?.error?.message || 'Đăng nhập thất bại. Vui lòng thử lại.';
        this.showAlert('login', msg, 'error');
      }
    });
}

  handleRegister(form: NgForm) {
    if (form.invalid) {
      form.form.markAllAsTouched();
      return;
    }

    if (this.registerData.password !== this.registerData.passwordConfirm) {
      this.showAlert('register', 'Mật khẩu xác nhận không khớp!', 'error');
      return;
    }

    this.isRegisterLoading = true;

    const payload: CustomerRegisterRequest = {
      email: this.registerData.email,
      password: this.registerData.password,
      passwordConfirm: this.registerData.passwordConfirm,
      fullName: this.registerData.fullName,
      phone: this.registerData.phone,
      dob: this.registerData.dob
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.isRegisterLoading = false;
        this.showAlert('register', 'Đăng ký thành công! Vui lòng đăng nhập.', 'success');
        setTimeout(() => this.switchTab('login'), 1500);
      },
      error: (err) => {
        this.isRegisterLoading = false;
        const msg = err?.error?.message || 'Đăng ký thất bại. Vui lòng thử lại.';
        this.showAlert('register', msg, 'error');
      }
    });
  }

  handleGoogleLogin() {
    this.showAlert('login', 'Đang kết nối với Google...', 'success');
    // Implement Google OAuth redirect here
  }
}