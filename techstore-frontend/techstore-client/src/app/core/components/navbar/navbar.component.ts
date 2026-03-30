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
  private isLoadingUser = false;

  constructor(
    private router: Router,
    private tokenService: TokenService,
    private authService: AuthService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.isLightTheme = document.body.classList.contains('light-theme');

    this.tokenService.getLoggedIn()
      .pipe(takeUntil(this.destroy$))
      .subscribe(status => {
        this.isLoggedIn = status;

      if (status && !this.customerService.currentUser && !this.isLoadingUser) {
        this.isLoadingUser = true;
        this.loadUserFromToken();
      }

        if (!status) {
          this.customerName = '';
          this.avatarUrl = null;
        }
      });

    this.customerService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.customerName = user.fullName;
          this.avatarUrl = user.avatarUrl;
        }
      });
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
      const customerId = Number(this.tokenService.getUserId());

      if (customerId) {
        this.customerService.loadCurrentUser(customerId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => this.isLoadingUser = false,
          error: () => {
            this.isLoadingUser = false;
            this.tokenService.removeToken();
          }
        });
      } else {
      this.isLoadingUser = false;
      this.tokenService.removeToken();
    }
    } catch {
      this.tokenService.removeToken();
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

  this.router.navigate(['/search'], {
    queryParams: { keyword: kw || null },
    queryParamsHandling: 'merge'
  });
}

  goProfile(): void { this.router.navigate(['/profile']); }
  goOrders(): void { this.router.navigate(['/orders']); }
  goLogin(): void { this.router.navigate(['/auth'], { queryParams: { tab: 'login' } }); }
  goRegister(): void { this.router.navigate(['/auth'], { queryParams: { tab: 'register' } }); }

  handleLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.customerService.clearCurrentUser();
        this.customerName = '';
        this.avatarUrl = null;
        this.isLoadingUser = false;
        this.router.navigate(['/home']);
      },
      error: () => {
        this.tokenService.removeToken();
        this.customerService.clearCurrentUser();
        this.isLoadingUser = false;
        this.router.navigate(['/home']);
      }
    });
  }

  handleImageError(event: any) {
    event.target.src = 'images/avatar_men.png';
  }
}