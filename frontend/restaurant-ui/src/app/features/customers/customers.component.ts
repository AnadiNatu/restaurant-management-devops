import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerResponse, CustomerType } from '../../core/models/model';
import { CustomerService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';
 
const CUSTOMER_TYPES: CustomerType[] = ['WALK_IN', 'DINE_IN', 'ORDER'];
@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.css'
})
export class CustomersComponent implements OnInit {
private custSvc = inject(CustomerService);
  private toast   = inject(ToastService);
  private fb      = inject(FormBuilder);
 
  customerTypes = CUSTOMER_TYPES;
  loading      = signal(true);
  saving       = signal(false);
  showModal    = signal(false);
  editingItem  = signal<CustomerResponse | null>(null);
  deleteTarget = signal<CustomerResponse | null>(null);
  items        = signal<CustomerResponse[]>([]);
  searchQuery  = signal('');
  typeFilter   = signal('');
 
  filtered = computed(() => {
    let list = this.items();
    const q = this.searchQuery().toLowerCase();
    if (q) list = list.filter(c =>
      c.name.toLowerCase().includes(q) ||
      c.email?.toLowerCase().includes(q) ||
      c.phone?.includes(q)
    );
    if (this.typeFilter()) list = list.filter(c => c.customerType === this.typeFilter());
    return list;
  });
 
  form = this.fb.group({
    name:         ['', Validators.required],
    email:        [''],
    phone:        [''],
    customerType: ['WALK_IN' as CustomerType],
    address:      [''],
    gstNumber:    ['']
  });
 
  typeClass(type: CustomerType): string {
    const map: Record<CustomerType, string> = {
      WALK_IN: 'badge-info',
      DINE_IN: 'badge-success',
      ORDER:   'badge-amber'
    };
    return map[type] ?? 'badge-info';
  }
 
  ngOnInit(): void { this.load(); }
 
  load(): void {
    this.custSvc.getAll().subscribe({
      next:  items => { this.items.set(items); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }
 
  openCreate(): void {
    this.editingItem.set(null);
    this.form.reset({ customerType: 'WALK_IN' });
    this.showModal.set(true);
  }
 
  openEdit(c: CustomerResponse): void {
    this.editingItem.set(c);
    this.form.patchValue({
      name: c.name, email: c.email ?? '', phone: c.phone ?? '',
      customerType: c.customerType, address: c.address ?? '', gstNumber: c.gstNumber ?? ''
    });
    this.showModal.set(true);
  }
 
  closeModal(): void { this.showModal.set(false); }
 
  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    const val = this.form.getRawValue() as any;
    const editing = this.editingItem();
 
    const obs$ = editing
      ? this.custSvc.update(editing.id, val)
      : this.custSvc.create(val);
 
    obs$.subscribe({
      next: saved => {
        if (editing) this.items.update(l => l.map(c => c.id === saved.id ? saved : c));
        else         this.items.update(l => [saved, ...l]);
        this.toast.success(editing ? 'Customer updated.' : 'Customer created.');
        this.closeModal();
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
 
  confirmDelete(c: CustomerResponse): void { this.deleteTarget.set(c); }
 
  deleteItem(): void {
    const c = this.deleteTarget();
    if (!c) return;
    this.saving.set(true);
    this.custSvc.delete(c.id).subscribe({
      next: () => {
        this.items.update(l => l.filter(x => x.id !== c.id));
        this.toast.success(`"${c.name}" deleted.`);
        this.deleteTarget.set(null);
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
}
