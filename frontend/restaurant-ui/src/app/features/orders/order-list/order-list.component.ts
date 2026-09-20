import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OrderResponse, OrderStatus } from '../../../core/models/model';
import { OrderService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

const STATUS_LIST: OrderStatus[] = ['PENDING','CONFIRMED','PREPARING','READY','SERVED','BILLED','CANCELLED'];

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.css'
})
export class OrderListComponent implements OnInit{
  readonly auth = inject(AuthService);
  private orderSvc = inject(OrderService);
  private toast    = inject(ToastService);
 
  statuses = STATUS_LIST;
  loading     = signal(true);
  orders      = signal<OrderResponse[]>([]);
  statusFilter = signal<string>('');
  searchQuery  = signal('');
 
  filtered = computed(() => {
    let list = this.orders();
    if (this.statusFilter()) list = list.filter(o => o.status === this.statusFilter());
    const q = this.searchQuery().toLowerCase();
    if (q) list = list.filter(o =>
      o.customerName?.toLowerCase().includes(q) ||
      o.tableNumber?.toLowerCase().includes(q) ||
      o.handledBy?.toLowerCase().includes(q)
    );
    return list;
  });
 
  countByStatus(s: OrderStatus): number {
    return this.orders().filter(o => o.status === s).length;
  }
 
  canCancel(order: OrderResponse): boolean {
    return order.status !== 'CANCELLED' && order.status !== 'BILLED';
  }
 
  ngOnInit(): void {
    const obs$ = this.auth.isAdmin() ? this.orderSvc.getAll() : this.orderSvc.getMyOrders();
    obs$.subscribe({
      next:  orders => { this.orders.set(orders); this.loading.set(false); },
      error: ()     => this.loading.set(false)
    });
  }
 
  cancelOrder(order: OrderResponse): void {
    if (!confirm(`Cancel order #${order.id}?`)) return;
    this.orderSvc.cancel(order.id).subscribe({
      next: () => {
        this.orders.update(list => list.map(o =>
          o.id === order.id ? { ...o, status: 'CANCELLED' as OrderStatus } : o
        ));
        this.toast.success(`Order #${order.id} cancelled.`);
      }
    });
  }

// Make use of "getMyOrders(): Observable<OrderResponse[]>" , "getByStatus(status: string): Observable<OrderResponse[]>" &
// getByCustomer(id: number): Observable<OrderResponse[]>
}