import { Routes } from '@angular/router';
import { BaseLayoutComponent } from './layouts/base-layout/base-layout.component';
import { FullLayoutComponent } from './layouts/full-layout/full-layout.component';
import { BlankLayoutComponent } from './layouts/blank-layout/blank-layout.component';

export const routes: Routes = [

   // ─── Base layout (Navbar + Footer) ──────────────────────────────
  {
    path: '',
    component: FullLayoutComponent,
    children: [
      {
        path: 'home',
        loadComponent: () =>
          import('./features/home/home.component').then(m => m.HomeComponent)
      }
    ]
  },

  
  // ─── Auth layout (chỉ content) ──────────────────────────────────
  {
    path: '',
    component: BlankLayoutComponent,
    children: [
      {
        path: 'auth',
        loadComponent: () =>
          import('./features/auth/auth.component').then(m => m.LoginComponent)
      }
    ]
  },

 

  // ─── Full layout (Navbar + Footer + Sidebar + Chatbot) ──────────
  // {
  //   path: '',
  //   component: FullLayoutComponent,
  //   children: [
  //     {
  //       path: 'dashboard',
  //       loadComponent: () =>
  //         import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  //     }
  //   ]
  // },

  // ─── Fallback ───────────────────────────────────────────────────
  {
    path: '**',
    redirectTo: ''
  }
];