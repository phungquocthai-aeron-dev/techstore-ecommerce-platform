// navbar.component.ts
import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TokenService } from '../../services/token.service';
import { AuthService } from '../../services/auth.service';
import { CustomerService } from '../../../features/user/customer.service';
import { CategoryResponse } from '../../../features/product/models/category.model';
import { CategoryService } from '../../../features/product/category.service';
import { NotificationService } from './notification.service';
import { NotificationResponse } from './models/notification.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {

  @ViewChild('notifScrollContainer') notifScrollContainer!: ElementRef<HTMLElement>;

  isLightTheme = false;
  isMobileMenuOpen = false;
  searchKeyword = '';
  isLoggedIn = false;

  customerName = '';
  avatarUrl: string | null = null;

  private destroy$ = new Subject<void>();
  private isLoadingUser = false;

  categoryGroups: { type: string; label: string; icon: string; items: CategoryResponse[] }[] = [];

  private readonly TYPE_META: Record<string, { label: string; icon: string }> = {
    LAPTOP:       { label: 'Laptop',       icon: 'bi-laptop' },
    SMARTPHONE:   { label: 'Điện thoại',   icon: 'bi-phone' },
    PC_COMPONENT: { label: 'Linh kiện PC', icon: 'bi-cpu' },
    ACCESSORY:    { label: 'Phụ kiện',     icon: 'bi-headphones' },
  };

  // ─── Notification state ───────────────────────────────────────────
  showNotifPanel = false;
  notifications: NotificationResponse[] = [];
  unreadCount = 0;

  notifLoadingInitial = false;
  notifLoadingMore = false;
  notifCurrentPage = 1;
  notifTotalPages = 1;
  notifPageSize = 10;

  /** IDs đã được mark-as-read trong session (tránh gọi API lặp lại) */
  private markedReadIds = new Set<string>();

  /** Tracking scroll để tránh gọi API liên tục */
  private isScrollFetching = false;

  private hasLoadedNotifications = false;

  constructor(
    private router: Router,
    private tokenService: TokenService,
    private authService: AuthService,
    private customerService: CustomerService,
    private categoryService: CategoryService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
      this.isLightTheme = document.body.classList.contains('light-theme');
      this.loadCategories();

      this.tokenService.getLoggedIn()
    .pipe(takeUntil(this.destroy$))
    .subscribe(status => {
      this.isLoggedIn = status;

      if (status) {
        if (!this.customerService.currentUser && !this.isLoadingUser) {
          this.isLoadingUser = true;
          this.loadUserFromToken();
        }
      
        if (!this.hasLoadedNotifications) {
          this.hasLoadedNotifications = true;
          this.loadNotificationsInitial();
        }
      }

      if (!status) {
        this.customerName = '';
        this.avatarUrl = null;
        this.resetNotifState();
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

    // Subscribe unread count từ service
    this.notificationService.unreadCount
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─── User loading ─────────────────────────────────────────────────

  private loadUserFromToken(): void {
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

  // ─── Categories ───────────────────────────────────────────────────

  private loadCategories(): void {
    this.categoryService.getAll({ size: 100 }).subscribe({
      next: res => {
        const all = res.result?.content ?? [];
        const grouped = new Map<string, CategoryResponse[]>();

        all.forEach(cat => {
          if (!grouped.has(cat.categoryType)) grouped.set(cat.categoryType, []);
          grouped.get(cat.categoryType)!.push(cat);
        });

        this.categoryGroups = Array.from(grouped.entries())
          .filter(([type]) => this.TYPE_META[type])
          .map(([type, items]) => ({
            type,
            label: this.TYPE_META[type].label,
            icon:  this.TYPE_META[type].icon,
            items
          }));
      }
    });
  }

  // ─── Notification Panel ───────────────────────────────────────────

  toggleNotifPanel(): void {
    if (this.showNotifPanel) {
      this.closeNotifPanel();
    } else {
      this.openNotifPanel();
    }
  }

  openNotifPanel(): void {
    this.showNotifPanel = true;

    if (this.notifications.length === 0) {
      this.loadNotificationsInitial();
    }
  }

  closeNotifPanel(): void {
    this.showNotifPanel = false;
  }

  private resetNotifState(): void {
    this.notifications = [];
    this.notifCurrentPage = 1;
    this.notifTotalPages = 1;
    this.unreadCount = 0;
    this.markedReadIds.clear();
    this.showNotifPanel = false;
    this.hasLoadedNotifications = false;
  }

  /** Load trang đầu tiên khi mở panel */
  private loadNotificationsInitial(): void {
    this.notifLoadingInitial = true;
    this.notifCurrentPage = 1;

    this.notificationService.getMyPosts(1, this.notifPageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const page = res.result;
          if (page) {
            this.notifications = page.data ?? [];
            this.notifTotalPages = page.totalPages;
            this.notifCurrentPage = page.currentPage;

            // Tính unread count từ data
            const unread = this.notifications.filter(n => !n.isRead).length;
            this.notificationService.setUnreadCount(unread);
          }
          this.notifLoadingInitial = false;
        },
        error: () => {
          this.notifLoadingInitial = false;
        }
      });
  }

  /** Load thêm khi scroll xuống */
  private loadNotificationsMore(): void {
    if (this.notifLoadingMore || this.isScrollFetching) return;
    if (this.notifCurrentPage >= this.notifTotalPages) return;

    this.notifLoadingMore = true;
    this.isScrollFetching = true;

    const nextPage = this.notifCurrentPage + 1;

    this.notificationService.getMyPosts(nextPage, this.notifPageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const page = res.result;
          if (page) {
            this.notifications = [...this.notifications, ...(page.data ?? [])];
            this.notifCurrentPage = page.currentPage;
            this.notifTotalPages = page.totalPages;
          }
          this.notifLoadingMore = false;
          this.isScrollFetching = false;
        },
        error: () => {
          this.notifLoadingMore = false;
          this.isScrollFetching = false;
        }
      });
  }

  /** Scroll handler: load more khi gần cuối + mark-as-read khi scroll qua */
  onNotifScroll(event: Event): void {
    const el = event.target as HTMLElement;
    const threshold = 80; // px từ cuối

    // Trigger load more
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - threshold) {
      this.loadNotificationsMore();
    }

    // Mark visible unread items as read
    this.markVisibleAsRead(el);
  }

  /**
   * Đánh dấu các thông báo unread đang hiển thị trong viewport là đã đọc.
   * Dựa trên vị trí scroll của container.
   */
  private markVisibleAsRead(container: HTMLElement): void {
    const items = container.querySelectorAll<HTMLElement>('.notif-item.unread');
    const containerTop = container.scrollTop;
    const containerBottom = containerTop + container.clientHeight;

    items.forEach(itemEl => {
      const itemTop = itemEl.offsetTop;
      const itemBottom = itemTop + itemEl.offsetHeight;

      // Kiểm tra item có trong viewport của scroll container không
      if (itemBottom > containerTop && itemTop < containerBottom) {
        const id = itemEl.getAttribute('data-id');
        if (id && !this.markedReadIds.has(id)) {
          this.markedReadIds.add(id);
          this.doMarkAsRead(id);
        }
      }
    });
  }

  /** Click vào một notification item */
  onNotifItemClick(n: NotificationResponse): void {
    if (!n.isRead && !this.markedReadIds.has(n.id)) {
      this.markedReadIds.add(n.id);
      this.doMarkAsRead(n.id);
    }
  }

  private doMarkAsRead(id: string): void {
    // Update local state immediately
    const notif = this.notifications.find(n => n.id === id);
    if (notif && !notif.isRead) {
      notif.isRead = true;
      this.notificationService.decrementUnread(1);
    }

    // Gọi API
    this.notificationService.markAsRead(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ error: () => {
        // Rollback nếu API lỗi
        if (notif) notif.isRead = false;
        this.markedReadIds.delete(id);
      }});
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notifications.forEach(n => n.isRead = true);
          this.markedReadIds.clear();
        }
      });
  }

  /** Format ngày giờ thân thiện */
  formatNotifDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffMin < 1)   return 'Vừa xong';
    if (diffMin < 60)  return `${diffMin} phút trước`;
    if (diffHour < 24) return `${diffHour} giờ trước`;
    if (diffDay < 7)   return `${diffDay} ngày trước`;

    return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  // ─── Navigation ───────────────────────────────────────────────────

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
        this.resetNotifState();
        this.router.navigate(['/home']);
      },
      error: () => {
        this.tokenService.removeToken();
        this.customerService.clearCurrentUser();
        this.isLoadingUser = false;
        this.resetNotifState();
        this.router.navigate(['/home']);
      }
    });
  }

  handleImageError(event: any): void {
    event.target.src = 'images/avatar_men.png';
  }

  goCategory(type: string, categoryId?: number): void {
    this.router.navigate(['/products'], {
      queryParams: { categoryType: type, ...(categoryId ? { categoryId } : {}) }
    });
  }

  goPromotion(): void {
    const offcanvasElement = document.getElementById('menuCanvas');
    if (offcanvasElement) {
      const bsOffcanvas = (window as any).bootstrap.Offcanvas.getInstance(offcanvasElement);
      bsOffcanvas?.hide();
    }
    this.router.navigate(['/promotions']);
  }
}