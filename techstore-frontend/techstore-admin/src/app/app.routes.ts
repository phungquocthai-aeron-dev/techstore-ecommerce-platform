import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { BlankLayoutComponent } from './layouts/blank-layout/blank-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [

  // ─── Full layout (có Navbar + Sidebar) ──────────────────────────
  {
    path: '',
    component: AdminLayoutComponent,
    canActivate: [authGuard],   // Bắt buộc đăng nhập
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      },
      // {
      //   path: 'dashboard',
      //   canActivate: [roleGuard],
      //   loadComponent: () =>
      //     import('./features/dashboard/dashboard.component')
      //       .then(m => m.DashboardComponent)
      // },
      {
        path: 'home',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/product/product.component')
            .then(m => m.ProductManagementComponent)
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/profile/profile.component')
            .then(m => m.ProfileComponent)
      },
      {
        path: 'orders',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/order/order.component')
            .then(m => m.OrderManagementComponent)
      },
      {
        path: 'categories',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/category/category.component')
            .then(m => m.CategoryManagementComponent)
      },
      {
        path: 'brands',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/brand/brand.component')
            .then(m => m.BrandManagementComponent)
      },
      {
        path: 'coupons',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/coupon/coupon.component')
            .then(m => m.CouponComponent)
      },
      {
        path: 'customers',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/customer/customer.component')
            .then(m => m.CustomerManagementComponent)
      },
      {
        path: 'staffs',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/staff/staff.component')
            .then(m => m.StaffManagementComponent)
      },
      {
        path: 'suppliers',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/supplier/supplier.component')
            .then(m => m.SupplierComponent)
      },
      {
        path: 'warehouses',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/warehouse/warehouse.component')
            .then(m => m.WarehouseComponent)
      },
      {
        path: 'warehouses/:id',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/warehouse/warehouse-detail.component')
            .then(m => m.WarehouseDetailComponent)
      },
      {
        path: 'reviews',
        canActivate: [roleGuard],
        loadComponent: () =>
          import('./features/review/review.component')
            .then(m => m.ReviewComponent)
      },
    ]
  },

  // ─── Blank layout (Auth) ─────────────────────────────────────────
  {
    path: 'auth',
    component: BlankLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/login/login.component')
            .then(m => m.LoginComponent)
      }
    ]
  },

  // ─── Fallback ────────────────────────────────────────────────────
  {
    path: '**',
    redirectTo: 'home'
  }
];