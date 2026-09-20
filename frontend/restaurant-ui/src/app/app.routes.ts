import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/gaurds/auth.gaurds';

export const routes: Routes = [
  // ── Auth (guest only) ─────────────────────────────────────
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.authRoutes)
  },

  // ── App Shell (authenticated) ─────────────────────────────
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/components/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'menu',
        loadComponent: () => import('./features/menu/menu.component').then(m => m.MenuComponent)
      },
      {
        path: 'orders',
        loadComponent: () => import('./features/orders/order-list/order-list.component').then(m => m.OrderListComponent)
      },
      {
        path: 'orders/create',
        loadComponent: () => import('./features/orders/create-order/create-order.component').then(m => m.CreateOrderComponent)
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent)
      },
      {
        path: 'inventory',
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN')],
        loadComponent: () => import('./features/inventory/inventory.component').then(m => m.InventoryComponent)
      },
      {
        path: 'customers',
        loadComponent: () => import('./features/customers/customers.component').then(m => m.CustomersComponent)
      },
      {
        path: 'employees',
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN')],
        loadComponent: () => import('./features/employees/employees.component').then(m => m.EmployeesComponent)
      },
      {
        path: 'reports',
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN')],
        loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent)
      },
      {
        path: 'activity-logs',
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN')],
        loadComponent: () => import('./features/activity-logs/activity-logs.component').then(m => m.ActivityLogsComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
      }
    ]
  },

  { path: '**', redirectTo: 'dashboard' }
];