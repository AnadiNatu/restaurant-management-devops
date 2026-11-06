import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConsumeRequest, CostSummaryResponse, InventoryResponse, InventoryTransactionResponse, RestockRequest, UpdateInventoryRequest } from '../../core/models/model';
import { InventoryService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';

type ModalMode = 'create' | 'edit' | 'restock' | 'consume' | 'transactions' |null;

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

  consumeTarget = signal<InventoryResponse | null>(null);
  transactionTarget = signal<InventoryResponse | null>(null);
  transactions = signal<InventoryTransactionResponse[] | null>(null);
  transactionsLoading = signal(false);

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
  quantity: [null as number | null,[Validators.required, Validators.min(0.001)]],
  unitCost: [null as number | null,[Validators.required, Validators.min(0)]],
  supplier: [''],
  notes: ['']
});

consumeForm = this.fb.group({
  quantity: [null as number | null,[Validators.required, Validators.min(0.001)]],
  notes: ['',[Validators.required, Validators.minLength(3)]]
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

  this.itemForm.controls.quantity.enable();
  this.itemForm.controls.costPerUnit.enable();

  this.modalMode.set('create');
}
 
  openEdit(item: InventoryResponse): void {

  this.editingItem.set(item);

  this.itemForm.patchValue({
    name: item.name,
    unit: item.unit,
    quantity: item.quantity,
    minThreshold: item.minThreshold,
    costPerUnit: item.costPerUnit,
    category: item.category || '',
    supplier: item.supplier || ''
  });

  // Stock and cost must be changed through transactions.
  this.itemForm.controls.quantity.disable();
  this.itemForm.controls.costPerUnit.disable();

  this.modalMode.set('edit');
}
 
  openRestock(item: InventoryResponse): void {

  this.restockTarget.set(item);

  this.restockForm.reset();

  this.restockForm.patchValue({
    unitCost: item.costPerUnit
  });

  this.modalMode.set('restock');
}
 
  closeModal(): void {

  this.modalMode.set(null);

  this.itemForm.controls.quantity.enable();
  this.itemForm.controls.costPerUnit.enable();

  this.editingItem.set(null);
  this.restockTarget.set(null);
  this.consumeTarget.set(null);
  this.transactionTarget.set(null);
}

  saveItem(): void {

  if (this.itemForm.invalid) {
    this.itemForm.markAllAsTouched();
    return;
  }

  this.saving.set(true);

  const editing = this.editingItem();

  if (editing) {

    const val = this.itemForm.getRawValue();

    const request: UpdateInventoryRequest = {
      name: val.name ?? undefined,
      unit: val.unit ?? undefined,
      minThreshold: val.minThreshold ?? undefined,
      category: val.category || undefined,
      supplier: val.supplier || undefined
    };

    this.invSvc.update(editing.id, request).subscribe({
      next: saved => {
        this.items.update(list =>
          list.map(item =>item.id === saved.id ? saved : item));
        this.toast.success('Inventory item updated.');
        this.closeModal();
        this.saving.set(false);
      },

      error: () => {
        this.saving.set(false);
        this.toast.error('Unable to update inventory item.');
      }
    });

  } else {
    const val = this.itemForm.getRawValue();

    const request = {
      name: val.name!,
      unit: val.unit!,
      quantity: val.quantity!,
      minThreshold: val.minThreshold!,
      costPerUnit: val.costPerUnit!,
      category: val.category || undefined,
      supplier: val.supplier || undefined
    };

    this.invSvc.create(request).subscribe({

      next: saved => {
        this.items.update(list => [saved, ...list]);
        this.toast.success('Inventory item created.');
        this.closeModal();
        this.saving.set(false);
      },

      error: () => {
        this.saving.set(false);
        this.toast.error('Unable to create inventory item.');
      }
    });
  }
}

openTransactions(item: InventoryResponse): void {

  this.transactionTarget.set(item);

  this.transactions.set([]);

  this.transactionsLoading.set(true);

  this.modalMode.set('transactions');

  this.invSvc.getTransactions(item.id).subscribe({

    next: result => {

      this.transactions.set(result);

      this.transactionsLoading.set(false);
    },

    error: () => {

      this.transactionsLoading.set(false);

      this.toast.error(
        'Unable to load inventory transaction history.'
      );
    }
  });
}
 
  doRestock(): void {

  if (this.restockForm.invalid) {
    this.restockForm.markAllAsTouched();
    return;
  }

  const target = this.restockTarget();
  if (!target) return;
  this.saving.set(true);
  const val = this.restockForm.getRawValue();

  const request: RestockRequest = {
    quantity: val.quantity!,
    unitCost: val.unitCost!,
    supplier: val.supplier || undefined,
    notes: val.notes || undefined
  };

  this.invSvc.restock(target.id, request).subscribe({

    next: saved => {

      this.items.update(list =>
        list.map(item =>item.id === saved.id ? saved : item));

      this.toast.success(`${saved.name} restocked successfully.`);

      this.closeModal();
      this.saving.set(false);
    },
    error: () => {
      this.saving.set(false);
      this.toast.error('Unable to restock inventory.');
    }
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
    .map(([key, value]) => ({key,value}
  ));

}

openConsume(item: InventoryResponse): void {
  this.consumeTarget.set(item);
  this.consumeForm.reset();
  this.modalMode.set('consume');
}


doConsume(): void {

  if (this.consumeForm.invalid) {
    this.consumeForm.markAllAsTouched();
    return;
  }

  const target = this.consumeTarget();
  if (!target) return;
  const val = this.consumeForm.getRawValue();
  if (val.quantity! > target.quantity) {
    this.toast.error(
      `Insufficient stock. Available: ${target.quantity} ${target.unit}`
    );
    return;
  }

  this.saving.set(true);

  const request: ConsumeRequest = {
    quantity: val.quantity!,
    notes: val.notes!
  };

  this.invSvc.consume(target.id, request).subscribe({

    next: saved => {
      this.items.update(list =>
        list.map(item =>item.id === saved.id ? saved : item));

      this.toast.success(`${request.quantity} ${saved.unit} of ${saved.name} consumed successfully.`);
      this.closeModal();
      this.saving.set(false);
    },

    error: () => {
      this.saving.set(false);
      this.toast.error('Unable to record consumption. Please check available stock.');
    }
  });
}

formatTransactionType(type: string): string {

  return type
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, char => char.toUpperCase());
}

getTransactionColor(type: string): string {

  switch (type) {

    case 'OPENING_BALANCE':
    case 'PURCHASE':
    case 'RESTOCK':
      return 'success';

    case 'CONSUMPTION':
      return 'warning';

    case 'ADJUSTMENT':
      return 'info';

    default:
      return 'secondary';
  }
}
}

// Make use of "getLowStock(): Observable<InventoryResponse[]>" , 
// "getWeeklyCost(): Observable<CostSummaryResponse>" & "getMonthlyCost(): Observable<CostSummaryResponse>" methods