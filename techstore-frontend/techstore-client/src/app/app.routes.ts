import { Routes } from '@angular/router';
import { FullLayoutComponent } from './layouts/full-layout/full-layout.component';
import { BlankLayoutComponent } from './layouts/blank-layout/blank-layout.component';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [

  // ─── Full layout (Navbar + Footer) ──────────────────────────────
  {
    path: '',
    component: FullLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./features/home/home.component')
            .then(m => m.HomeComponent)
      },
      {
        path: 'product/:id',
        loadComponent: () =>
          import('./features/product-detail/product-detail.component')
            .then(m => m.ProductDetailComponent)
      },
      {
        path: 'search',
        loadComponent: () =>
          import('./features/search/search.component')
            .then(m => m.SearchComponent)
      },
      {
        path: 'cart',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/cart/cart.component')
            .then(m => m.CartComponent)
      },
      {
        path: 'checkout',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/check-out/check-out.component')
            .then(m => m.CheckoutComponent)
      },
      {
        path: 'orders',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/order/order.component')
            .then(m => m.OrdersComponent)
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/user/profile.component')
            .then(m => m.ProfileComponent)
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
        canActivate: [guestGuard],
        loadComponent: () =>
          import('./features/auth/auth.component')
            .then(m => m.LoginComponent)
      },
      {
        path: 'order-success',
        loadComponent: () =>
          import('./features/order-success/order-success.component')
            .then(m => m.OrderSuccessComponent)
      },
      {
        path: 'quizgame',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/quizgame/quizgame.component')
            .then(m => m.QuizGameComponent)
      },
    ]
  },

  // ─── Fallback ────────────────────────────────────────────────────
  {
    path: '**',
    redirectTo: 'home'
  }
];