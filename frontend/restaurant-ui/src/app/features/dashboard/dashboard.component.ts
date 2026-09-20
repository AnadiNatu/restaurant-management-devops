import { Component, inject, OnInit, signal } from '@angular/core';
import { DashboardSummary, OrderResponse } from '../../core/models/model';
import { ReportService, OrderService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
readonly auth = inject(AuthService);
  private reportSvc = inject(ReportService);
  private orderSvc  = inject(OrderService);
 
  today = new Date();
  loading       = signal(true);
  recentLoading = signal(true);
  summary       = signal<DashboardSummary | null>(null);
  recentOrders  = signal<OrderResponse[]>([]);
 
  firstName = () => {
    const name = this.auth.currentUser?.fullName ?? 'Chef';
    return name.split(' ')[0];
  };
 
  greeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  }
 
  ngOnInit(): void {
    if (this.auth.isAdmin()) {
      this.reportSvc.getDashboard().subscribe({
        next:  s => { this.summary.set(s); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
    } else {
      this.loading.set(false);
    }
 
    const orders$ = this.auth.isAdmin()
      ? this.orderSvc.getAll()
      : this.orderSvc.getMyOrders();
 
    orders$.subscribe({
      next: orders => {
        this.recentOrders.set(orders.slice(0, 8));
        this.recentLoading.set(false);
      },
      error: () => this.recentLoading.set(false)
    });
  }
}
