import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CostSummaryResponse, InventoryResponse } from '../../core/models/model';
import { InventoryService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';

type ModalMode = 'create' | 'edit' | 'restock' | null;

interface CostSummaryEntry {
  key: string;
  value: unknown;
}

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent implements OnInit {
  private invSvc = inject(InventoryService);
  private toast  = inject(ToastService);
  private fb     = inject(FormBuilder);
 
  loading       = signal(true);
  saving        = signal(false);
  items         = signal<InventoryResponse[]>([]);
  modalMode     = signal<ModalMode>(null);
  editingItem   = signal<InventoryResponse | null>(null);
  restockTarget = signal<InventoryResponse | null>(null);
  deleteTarget  = signal<InventoryResponse | null>(null);
  searchQuery   = signal('');
  catFilter     = signal('');
  showLowOnly   = signal(false);

  lowStockApiItems = signal<InventoryResponse[] | null>(null);
weeklyCost = signal<CostSummaryResponse | null>(null);
monthlyCost = signal<CostSummaryResponse | null>(null);
apiLoading = signal(false);
 
  lowStockCount = computed(() => this.items().filter(i => i.isLowStock).length);
  categories    = computed(() => [...new Set(this.items().map(i => i.category).filter(Boolean))].sort());
 
  filtered = computed(() => {
    let list = this.items();
    if (this.showLowOnly()) list = list.filter(i => i.isLowStock);
    if (this.catFilter()) list = list.filter(i => i.category === this.catFilter());
    const q = this.searchQuery().toLowerCase();
    if (q) list = list.filter(i => i.name.toLowerCase().includes(q) || i.supplier?.toLowerCase().includes(q));
    return list;
  });
 
  itemForm = this.fb.group({
    name:         ['', Validators.required],
    unit:         ['', Validators.required],
    quantity:     [null as number | null, [Validators.required, Validators.min(0)]],
    minThreshold: [null as number | null, [Validators.required, Validators.min(0)]],
    costPerUnit:  [null as number | null, [Validators.required, Validators.min(0.01)]],
    category:     [''],
    supplier:     ['']
  });
 
  restockForm = this.fb.group({
    quantity: [null as number | null, [Validators.required, Validators.min(0.001)]],
    supplier: ['']
  });
 
  ngOnInit(): void { this.load(); }
 
  load(): void {
    this.invSvc.getAll().subscribe({
      next:  items => { this.items.set(items); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }
 
  openCreate(): void {
    this.editingItem.set(null);
    this.itemForm.reset();
    this.modalMode.set('create');
  }
 
  openEdit(item: InventoryResponse): void {
    this.editingItem.set(item);
    this.itemForm.patchValue({ name: item.name, unit: item.unit, quantity: item.quantity, minThreshold: item.minThreshold, costPerUnit: item.costPerUnit, category: item.category || '', supplier: item.supplier || '' });
    this.modalMode.set('edit');
  }
 
  openRestock(item: InventoryResponse): void {
    this.restockTarget.set(item);
    this.restockForm.reset();
    this.modalMode.set('restock');
  }
 
  closeModal(): void { this.modalMode.set(null); }
 
  saveItem(): void {
    if (this.itemForm.invalid) { this.itemForm.markAllAsTouched(); return; }
    this.saving.set(true);
    const val = this.itemForm.getRawValue() as any;
    const editing = this.editingItem();
 
    const obs$ = editing
      ? this.invSvc.update(editing.id, val)
      : this.invSvc.create(val);
 
    obs$.subscribe({
      next: saved => {
        if (editing) this.items.update(l => l.map(i => i.id === saved.id ? saved : i));
        else         this.items.update(l => [saved, ...l]);
        this.toast.success(editing ? 'Item updated.' : 'Item created.');
        this.closeModal();
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
 
  doRestock(): void {
    if (this.restockForm.invalid) { this.restockForm.markAllAsTouched(); return; }
    const target = this.restockTarget();
    if (!target) return;
    this.saving.set(true);
    this.invSvc.restock(target.id, this.restockForm.getRawValue() as any).subscribe({
      next: saved => {
        this.items.update(l => l.map(i => i.id === saved.id ? saved : i));
        this.toast.success(`${saved.name} restocked.`);
        this.closeModal();
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
 
  confirmDelete(item: InventoryResponse): void { this.deleteTarget.set(item); }
 
  deleteItem(): void {
    const item = this.deleteTarget();
    if (!item) return;
    this.saving.set(true);
    this.invSvc.delete(item.id).subscribe({
      next: () => {
        this.items.update(l => l.filter(i => i.id !== item.id));
        this.toast.success(`"${item.name}" deleted.`);
        this.deleteTarget.set(null);
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }

  loadLowStock(): void {
  this.apiLoading.set(true);
  this.invSvc.getLowStock().subscribe({
    next: items => {
      this.lowStockApiItems.set(items);
      this.apiLoading.set(false);
    },
    error: () => {this.apiLoading.set(false);}
  });
}

loadWeeklyCost(): void {
  this.apiLoading.set(true);
  this.invSvc.getWeeklyCost().subscribe({
    next: summary => {
      this.weeklyCost.set(summary);
      this.apiLoading.set(false);
    },

    error: () => {this.apiLoading.set(false);}
  });
}

loadMonthlyCost(): void {
  this.apiLoading.set(true);
  this.invSvc.getMonthlyCost().subscribe({
    next: summary => {
      this.monthlyCost.set(summary);
      this.apiLoading.set(false);
    },

    error: () => {this.apiLoading.set(false);}
  });
}

getCostEntries(summary: CostSummaryResponse | null): CostSummaryEntry[] {

  if (!summary) {
    return [];
  }

  return Object.entries(summary)
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([key, value]) => ({
      key,
      value
    }));

}

}

// Make use of "getLowStock(): Observable<InventoryResponse[]>" , 
// "getWeeklyCost(): Observable<CostSummaryResponse>" & "getMonthlyCost(): Observable<CostSummaryResponse>" methods
