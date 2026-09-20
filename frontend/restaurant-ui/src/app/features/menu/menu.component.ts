import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MenuItemResponse, CreateMenuItemRequest } from '../../core/models/model';
import { MenuService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {
  readonly auth = inject(AuthService);
  private menuSvc = inject(MenuService);
  private fb      = inject(FormBuilder);
  private toast   = inject(ToastService);
 // Make use of the getById(id: number): Observable<MenuItemResponse>
// , getAvailable(): Observable<MenuItemResponse[]> & getByCategory(cat: string): Observable<MenuItemResponse[]>  
  loading      = signal(true);
  saving       = signal(false);
  showModal    = signal(false);
  editingItem  = signal<MenuItemResponse | null>(null);
  deleteTarget = signal<MenuItemResponse | null>(null);
  items        = signal<MenuItemResponse[]>([]);
  searchQuery  = signal('');
  selectedCategory = signal('');
  availableOnly    = signal(false);
 
  categories = computed(() =>
    [...new Set(this.items().map(i => i.category))].sort()
  );
 
  filtered = computed(() => {
    let list = this.items();
    const q = this.searchQuery().toLowerCase();
    if (q) list = list.filter(i => i.name.toLowerCase().includes(q) || i.category.toLowerCase().includes(q));
    if (this.selectedCategory()) list = list.filter(i => i.category === this.selectedCategory());
    if (this.availableOnly()) list = list.filter(i => i.isAvailable);
    return list;
  });
 
  itemForm = this.fb.group({
    name:        ['', Validators.required],
    description: [''],
    price:       [null as number | null, [Validators.required, Validators.min(0.01)]],
    category:    ['', Validators.required],
    isAvailable: [true],
    taxCategory: [''],
    taxRate:     [0]
  });
 
  ngOnInit(): void { this.load(); }
 
  load(): void {
    this.loading.set(true);
    this.menuSvc.getAll().subscribe({
      next:  items => { this.items.set(items); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }
 
  openCreate(): void {
    this.editingItem.set(null);
    this.itemForm.reset({ isAvailable: true, taxRate: 0 });
    this.showModal.set(true);
  }
 
  openEdit(item: MenuItemResponse): void {
    this.editingItem.set(item);
    this.itemForm.patchValue({
      name: item.name, description: item.description ?? '',
      price: item.price, category: item.category,
      isAvailable: item.isAvailable, taxCategory: item.taxCategory ?? '',
      taxRate: item.taxRate ?? 0
    });
    this.showModal.set(true);
  }
 
  closeModal(): void { this.showModal.set(false); }
 
  saveItem(): void {
    if (this.itemForm.invalid) { this.itemForm.markAllAsTouched(); return; }
    this.saving.set(true);
    const val = this.itemForm.getRawValue() as CreateMenuItemRequest;
    const editing = this.editingItem();
 
    const obs$ = editing
      ? this.menuSvc.update(editing.id, val)
      : this.menuSvc.create(val);
 
    obs$.subscribe({
      next: saved => {
        if (editing) {
          this.items.update(list => list.map(i => i.id === saved.id ? saved : i));
          this.toast.success('Menu item updated.');
        } else {
          this.items.update(list => [saved, ...list]);
          this.toast.success('Menu item created.');
        }
        this.closeModal();
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
 
  toggleAvailability(item: MenuItemResponse): void {
    this.menuSvc.toggleAvailability(item.id).subscribe({
      next: updated => {
        this.items.update(list => list.map(i => i.id === updated.id ? updated : i));
        this.toast.success(`"${updated.name}" is now ${updated.isAvailable ? 'available' : 'unavailable'}.`);
      }
    });
  }
 
  confirmDelete(item: MenuItemResponse): void { this.deleteTarget.set(item); }
 
  deleteItem(): void {
    const item = this.deleteTarget();
    if (!item) return;
    this.saving.set(true);
    this.menuSvc.delete(item.id).subscribe({
      next: () => {
        this.items.update(list => list.filter(i => i.id !== item.id));
        this.toast.success(`"${item.name}" deleted.`);
        this.deleteTarget.set(null);
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
}
