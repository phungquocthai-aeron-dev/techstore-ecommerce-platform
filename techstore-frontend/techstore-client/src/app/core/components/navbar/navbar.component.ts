import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TokenService } from '../../services/token.service';
import { AuthService } from '../../services/auth.service';
import { CustomerService } from '../../../features/user/customer.service'; 

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  isLightTheme = false;
  isMobileMenuOpen = false;
  searchKeyword = '';
  isLoggedIn = false;

  customerName = '';
  avatarUrl: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private tokenService: TokenService,
    private authService: AuthService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.isLightTheme = document.body.classList.contains('light-theme');
    this.isLoggedIn = this.tokenService.isLoggedIn();

    // Subscribe theo dõi thay đổi user (avatar update, profile update,...)
    this.customerService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.customerName = user.fullName;
          this.avatarUrl = user.avatarUrl;
          this.isLoggedIn = true;
        } else {
          this.isLoggedIn = this.tokenService.isLoggedIn();
        }
      });

    // Nếu đã có token nhưng chưa có user trong state → load từ API
    if (this.isLoggedIn && !this.customerService.currentUser) {
      this.loadUserFromToken();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadUserFromToken(): void {
    // Lấy customer ID từ token (JWT decode)
    const token = this.tokenService.getToken();
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const customerId = payload.sub ? Number(payload.sub) : null;

      if (customerId) {
        this.customerService.loadCurrentUser(customerId)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            error: () => {
              // Token hết hạn hoặc lỗi → clear
              this.tokenService.removeToken();
              this.isLoggedIn = false;
            }
          });
      }
    } catch {
      this.tokenService.removeToken();
      this.isLoggedIn = false;
    }
  }

  toggleTheme(): void {
    this.isLightTheme = !this.isLightTheme;
    document.body.classList.toggle('light-theme', this.isLightTheme);
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  goSearch(): void {
    const kw = this.searchKeyword.trim();
    if (kw) {
      this.router.navigate(['/search'], { queryParams: { keyword: kw } });
    } else {
      this.router.navigate(['/search']);
    }
  }

  goProfile(): void { this.router.navigate(['/profile']); }
  goOrders(): void { this.router.navigate(['/orders']); }
  goLogin(): void { this.router.navigate(['/auth'], { queryParams: { tab: 'login' } }); }
  goRegister(): void { this.router.navigate(['/auth'], { queryParams: { tab: 'register' } }); }

  handleLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.customerService.clearCurrentUser();
        this.isLoggedIn = false;
        this.customerName = '';
        this.avatarUrl = null;
        this.router.navigate(['/home']);
      },
      error: () => {
        this.tokenService.removeToken();
        this.customerService.clearCurrentUser();
        this.isLoggedIn = false;
        this.router.navigate(['/home']);
      }
    });
  }

  handleImageError(event: any) {
    event.target.src = 'images/avatar_men.png';
  }
}