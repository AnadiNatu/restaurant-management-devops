import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MenuItemResponse, OrderItemRequest } from '../../../core/models/model';
import { MenuService, OrderService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { CommonModule } from '@angular/common';

interface CartItem {
  menuItem: MenuItemResponse;
  quantity: number;
}
@Component({
  selector: 'app-create-order',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './create-order.component.html',
  styleUrl: './create-order.component.css'
})
export class CreateOrderComponent {
private menuSvc  = inject(MenuService);
  private orderSvc = inject(OrderService);
  private toast    = inject(ToastService);
  private router   = inject(Router);
  private fb       = inject(FormBuilder);
 
  menuLoading = signal(true);
  placing     = signal(false);
  menuItems   = signal<MenuItemResponse[]>([]);
  cart        = signal<CartItem[]>([]);
  menuSearch  = signal('');
  selectedCat = signal('');
 
  menuCategories = computed(() => [...new Set(this.menuItems().map(i => i.category))].sort());
 
  filteredMenu = computed(() => {
    let list = this.menuItems().filter(i => i.isAvailable);
    const q = this.menuSearch().toLowerCase();
    if (q) list = list.filter(i => i.name.toLowerCase().includes(q));
    if (this.selectedCat()) list = list.filter(i => i.category === this.selectedCat());
    return list;
  });
 
  subtotal     = computed(() => this.cart().reduce((s, c) => s + c.menuItem.price * c.quantity, 0));
  estimatedTax = computed(() => this.cart().reduce((s, c) => s + (c.menuItem.price * c.quantity * (c.menuItem.taxRate ?? 0) / 100), 0));
  discount     = computed(() => +(this.detailForm.value.discountAmount ?? 0));
  total        = computed(() => Math.max(0, this.subtotal() + this.estimatedTax() - this.discount()));
 
  detailForm = this.fb.group({
    tableNumber:     [''],
    customerName:    [''],
    customerEmail:   [''],
    discountAmount:  [0],
    notes:           ['']
  });
 
  ngOnInit(): void {
    this.menuSvc.getAvailable().subscribe({
      next:  items => { this.menuItems.set(items); this.menuLoading.set(false); },
      error: ()    => this.menuLoading.set(false)
    });
  }
 
  isInCart(id: number): boolean { return this.cart().some(c => c.menuItem.id === id); }
 
  addToCart(item: MenuItemResponse): void {
    const existing = this.cart().find(c => c.menuItem.id === item.id);
    if (existing) {
      this.cart.update(list => list.map(c =>
        c.menuItem.id === item.id ? { ...c, quantity: c.quantity + 1 } : c
      ));
    } else {
      this.cart.update(list => [...list, { menuItem: item, quantity: 1 }]);
    }
  }
 
  incQty(item: CartItem): void { this.cart.update(list => list.map(c => c.menuItem.id === item.menuItem.id ? { ...c, quantity: c.quantity + 1 } : c)); }
  decQty(item: CartItem): void {
    if (item.quantity <= 1) { this.removeFromCart(item); return; }
    this.cart.update(list => list.map(c => c.menuItem.id === item.menuItem.id ? { ...c, quantity: c.quantity - 1 } : c));
  }
 
  removeFromCart(item: CartItem): void {
    this.cart.update(list => list.filter(c => c.menuItem.id !== item.menuItem.id));
  }
 
  placeOrder(): void {
    if (this.cart().length === 0) { this.toast.warning('Add items to cart first.'); return; }
    this.placing.set(true);
    const fv = this.detailForm.getRawValue();
    const items: OrderItemRequest[] = this.cart().map(c => ({ menuItemId: c.menuItem.id, quantity: c.quantity }));
 
    this.orderSvc.create({
      tableNumber:    fv.tableNumber || undefined,
      customerName:   fv.customerName || undefined,
      customerEmail:  fv.customerEmail || undefined,
      discountAmount: fv.discountAmount || 0,
      notes:          fv.notes || undefined,
      items
    }).subscribe({
      next: order => {
        this.toast.success(`Order #${order.id} created!`);
        this.router.navigate(['/orders', order.id]);
      },
      error: () => this.placing.set(false)
    });
  }
}
