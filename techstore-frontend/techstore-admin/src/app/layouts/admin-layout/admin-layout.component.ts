import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { filter } from 'rxjs/operators';

import { SidebarComponent } from '../../core/components/sidebar/sidebar.component';

const PAGE_TITLES: Record<string, string> = {
  '/admin/dashboard':  'Tổng quan',
  '/admin/products':   'Quản lý sản phẩm',
  '/admin/categories': 'Danh mục',
  '/admin/brands':     'Thương hiệu',
  '/admin/warehouse':  'Kho hàng',
  '/admin/suppliers':  'Nhà cung cấp',
  '/admin/orders':     'Đơn đặt hàng',
  '/admin/coupons':    'Mã giảm giá',
  '/admin/payments':   'Thanh toán',
  '/admin/customers':  'Khách hàng',
  '/admin/employees':  'Nhân viên',
  '/admin/reviews':    'Bình luận',
};

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, SidebarComponent],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent implements OnInit {

  sidebarCollapsed = false;
  pageTitle = 'Tổng quan';
  searchQuery = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Cập nhật tiêu đề theo route
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        // Lấy path gốc (không query params)
        const path = e.urlAfterRedirects.split('?')[0];
        this.pageTitle = PAGE_TITLES[path] ?? 'AdminPro';
      });

    // Set tiêu đề cho route hiện tại khi mới load
    const current = this.router.url.split('?')[0];
    this.pageTitle = PAGE_TITLES[current] ?? 'AdminPro';
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}