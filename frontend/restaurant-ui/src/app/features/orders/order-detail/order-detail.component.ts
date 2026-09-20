import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BillResponse, MenuItemResponse, OrderItemRequest, OrderResponse, OrderStatus } from '../../../core/models/model';
import { MenuService, OrderService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';


const NEXT_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {
  PENDING:   'CONFIRMED',
  CONFIRMED: 'PREPARING',
  PREPARING: 'READY',
  READY:     'SERVED',
  SERVED:    'BILLED'
};

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css'
})
export class OrderDetailComponent implements OnInit{
   readonly auth = inject(AuthService);
  private route    = inject(ActivatedRoute);
  private orderSvc = inject(OrderService);
  private toast    = inject(ToastService);
 
  loading = signal(true);
  saving  = signal(false);
  order   = signal<OrderResponse | null>(null);
  bill    = signal<BillResponse | null>(null);
 
  ngOnInit(): void {
    const id = +this.route.snapshot.paramMap.get('id')!;
    this.orderSvc.getById(id).subscribe({
      next:  o => { this.order.set(o); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
 
  nextStatus(status: OrderStatus): OrderStatus | undefined {
    return NEXT_STATUS[status];
  }
 
  canBill(): boolean {
    return this.order()?.status === 'SERVED';
  }
 
  canCancel(): boolean {
    const s = this.order()?.status;
    return !!s && s !== 'CANCELLED' && s !== 'BILLED';
  }
 
  advance(): void {
    const o = this.order();
    if (!o) return;
    const next = this.nextStatus(o.status);
    if (!next) return;
    this.saving.set(true);
    this.orderSvc.updateStatus(o.id, { status: next }).subscribe({
      next: updated => { this.order.set(updated); this.saving.set(false); this.toast.success(`Status updated to ${next}.`); },
      error: () => this.saving.set(false)
    });
  }
 
  generateBill(): void {
    const o = this.order();
    if (!o) return;
    this.saving.set(true);
    this.orderSvc.generateBill(o.id).subscribe({
      next: bill => {
        this.bill.set(bill);
        this.order.update(prev => prev ? { ...prev, status: 'BILLED' as OrderStatus } : prev);
        this.saving.set(false);
        this.toast.success(`Invoice ${bill.invoiceNumber} generated!`);
      },
      error: () => this.saving.set(false)
    });
  }
 
  cancel(): void {
    const o = this.order();
    if (!o || !confirm(`Cancel order #${o.id}?`)) return;
    this.saving.set(true);
    this.orderSvc.cancel(o.id).subscribe({
      next: () => {
        this.order.update(prev => prev ? { ...prev, status: 'CANCELLED' as OrderStatus } : prev);
        this.saving.set(false);
        this.toast.success('Order cancelled.');
      },
      error: () => this.saving.set(false)
    });
  }
}
