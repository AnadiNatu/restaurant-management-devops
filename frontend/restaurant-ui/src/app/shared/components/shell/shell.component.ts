import { Component, computed, HostListener, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}
 
const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard',      icon: '◉',  route: '/dashboard' },
  { label: 'Orders',         icon: '🧾', route: '/orders' },
  { label: 'Menu',           icon: '🍽️', route: '/menu' },
  { label: 'Customers',      icon: '👥', route: '/customers' },
  { label: 'Inventory',      icon: '📦', route: '/inventory',      roles: ['SUPER_ADMIN','ADMIN'] },
  { label: 'Employees',      icon: '🧑‍💼', route: '/employees',     roles: ['SUPER_ADMIN','ADMIN'] },
  { label: 'Reports',        icon: '📊', route: '/reports',        roles: ['SUPER_ADMIN','ADMIN'] },
  { label: 'Activity Logs',  icon: '📋', route: '/activity-logs',  roles: ['SUPER_ADMIN','ADMIN'] },
];

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  private router = inject(Router);
 
  collapsed  = signal(false);
  mobileOpen = signal(false);
 
  visibleNav = computed(() => {
    const role = this.auth.role;
    return NAV_ITEMS.filter(item =>
      !item.roles || (role && item.roles.includes(role))
    );
  });
 
  userInitials = computed(() => {
    const user = this.auth.currentUser;
    if (!user) return '?';
    return user.fullName.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
  });
 
  getCurrentPageLabel(): string {
    const url   = this.router.url.split('?')[0];
    const item  = NAV_ITEMS.find(n => url.startsWith(n.route));
    if (url === '/profile') return 'Profile';
    return item?.label ?? 'Bistro Hub';
  }
 
  @HostListener('window:resize')
  onResize(): void {
    if (window.innerWidth > 900) this.mobileOpen.set(false);
  }
 
  logout(): void { this.auth.logout(); }
}
