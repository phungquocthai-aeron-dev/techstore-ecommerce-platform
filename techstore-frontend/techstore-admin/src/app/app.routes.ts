import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { BlankLayoutComponent } from './layouts/blank-layout/blank-layout.component';

export const routes: Routes = [

  // ─── Full layout (Navbar + Footer) ──────────────────────────────
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./features/product/product.component')
            .then(m => m.ProductManagementComponent)
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/order/order.component')
            .then(m => m.OrderManagementComponent)
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/category/category.component')
            .then(m => m.CategoryManagementComponent)
      },
      {
        path: 'brands',
        loadComponent: () =>
          import('./features/brand/brand.component')
            .then(m => m.BrandManagementComponent)
      },
      {
        path: 'coupons',
        loadComponent: () =>
          import('./features/coupon/coupon.component')
            .then(m => m.CouponComponent)
      },
      {
        path: 'customers',
        loadComponent: () =>
          import('./features/customer/customer.component')
            .then(m => m.CustomerManagementComponent)
      },
      {
        path: 'staffs',
        loadComponent: () =>
          import('./features/staff/staff.component')
            .then(m => m.StaffManagementComponent)
      },
      {
        path: 'suppliers',
        loadComponent: () =>
          import('./features/supplier/supplier.component')
            .then(m => m.SupplierComponent)
      },
      {
        path: 'warehouses',
        loadComponent: () =>
          import('./features/warehouse/warehouse.component')
            .then(m => m.WarehouseComponent)
      },
      {
        path: 'warehouses/:id',
        loadComponent: () =>
          import('./features/warehouse/warehouse-detail.component')
            .then(m => m.WarehouseDetailComponent)
      },
      {
        path: 'reviews',
        loadComponent: () =>
          import('./features/review/review.component')
            .then(m => m.ReviewComponent)
      },
    ]
  },

  // ─── Blank layout (Auth) ─────────────────────────────────────────
  {
    path: '',
    component: BlankLayoutComponent,
    children: [
      {
        path: 'auth',
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