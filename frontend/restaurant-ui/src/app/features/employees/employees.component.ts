import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserResponse, UserRole, CreateUserRequest } from '../../core/models/model';
import { UserManagementService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.css'
})
export class EmployeesComponent implements OnInit{
readonly auth = inject(AuthService);
  private userSvc = inject(UserManagementService);
  private toast   = inject(ToastService);
  private fb      = inject(FormBuilder);
 
  loading      = signal(true);
  saving       = signal(false);
  showModal    = signal(false);
  editingUser  = signal<UserResponse | null>(null);
  deleteTarget = signal<UserResponse | null>(null);
  users        = signal<UserResponse[]>([]);
  searchQuery  = signal('');
  roleFilter   = signal('');
  activeOnly   = signal(false);

  lookupUserId = signal('');
  lookupEmployeeId = signal('');
  lookupRole = signal<UserRole | ''>('');

  lookupLoading = signal(false);
 
  filtered = computed(() => {
    let list = this.users();
    const q = this.searchQuery().toLowerCase();
    if (q) list = list.filter(u =>
      `${u.fname} ${u.lname}`.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q)
    );
    if (this.roleFilter()) list = list.filter(u => u.role === this.roleFilter());
    if (this.activeOnly()) list = list.filter(u => u.isActive);
    return list;
  });
 
  userForm = this.fb.group({
    fname: ['', Validators.required],
    lname: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    phoneNumber: [''],
    role: ['EMPLOYEE' as UserRole]
  });
 
  initials(u: UserResponse): string {
    return `${u.fname[0] ?? ''}${u.lname[0] ?? ''}`.toUpperCase();
  }
 
  roleClass(role: UserRole): string {
    const map: Record<UserRole, string> = {
      SUPER_ADMIN: 'badge-super',
      ADMIN:       'badge-amber',
      EMPLOYEE:    'badge-info'
    };
    return map[role] ?? 'badge-info';
  }
 
  ngOnInit(): void { this.load(); }
 
  load(): void {
    const obs$ = this.auth.isSuperAdmin()
      ? this.userSvc.getAllUsers()
      : this.userSvc.getEmployees();
 
    obs$.subscribe({
      next:  users => { this.users.set(users); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }

  // GET USER BY ID
   loadUserById(): void {
    const id = Number(this.lookupUserId());

    if (!id || id <= 0) {
      this.toast.warning('Please enter a valid user ID.');
      return;
    }
    this.lookupLoading.set(true);
    this.userSvc.getUserById(id).subscribe({
      next: user => {
        /*
         * Keep the result compatible with the existing
         * users[] based UI.
         */
        this.users.set([user]);
        this.lookupLoading.set(false);
        this.toast.success(`User #${id} loaded.`);
      },
      error: () => { this.lookupLoading.set(false);}
    });
  }

  // GET USERS BY ROLE
  loadUsersByRole(): void {
    const role = this.lookupRole();
    if (!role) {
      this.toast.warning('Please select a role.');
      return;
    }

    this.lookupLoading.set(true);
    this.userSvc.getUsersByRole(
      role as UserRole).subscribe({
      next: users => {
        this.users.set(users);
        this.lookupLoading.set(false);
        this.toast.success(`${users.length} user(s) with role ${role} loaded.`);
      },

      error: () => {this.lookupLoading.set(false);}
    });
  }

  // GET EMPLOYEE BY ID
  loadEmployeeById(): void {
    const id = Number(this.lookupEmployeeId());

    if (!id || id <= 0) {
      this.toast.warning('Please enter a valid employee ID.');
      return;
    }
    this.lookupLoading.set(true);
    this.userSvc.getEmployeeById(id).subscribe({
      next: users => {
        /*
         * Service currently returns UserResponse[].
         * Keep it as an array so the existing users grid
         * can display the result without changing its model.
         */
        this.users.set(users);
        this.lookupLoading.set(false);
        this.toast.success(`Employee #${id} loaded.`);
      },

      error: () => {this.lookupLoading.set(false);}
    });
  }

  // CLEAR LOOKUP
  clearLookup(): void {
    this.lookupUserId.set('');
    this.lookupEmployeeId.set('');
    this.lookupRole.set('');
    this.load();
  }
 
  openCreate(): void {
    this.editingUser.set(null);
    this.userForm.reset({ role: 'EMPLOYEE' });
    this.userForm.controls['email'].enable();
    this.userForm.controls['password'].setValidators([Validators.required, Validators.minLength(8)]);
    this.userForm.controls['password'].updateValueAndValidity();
    this.showModal.set(true);
  }
 
  openEdit(user: UserResponse): void {
    this.editingUser.set(user);
    this.userForm.patchValue({
      fname: user.fname, lname: user.lname,
      email: user.email, phoneNumber: user.phoneNumber ?? '',
      role: user.role, password: ''
    });
    this.userForm.controls['email'].disable();
    this.userForm.controls['password'].clearValidators();
    this.userForm.controls['password'].updateValueAndValidity();
    this.showModal.set(true);
  }
 
  closeModal(): void { this.showModal.set(false); }
 
  save(): void {
    const editing = this.editingUser();
    if (!editing && this.userForm.invalid) { this.userForm.markAllAsTouched(); return; }
    this.saving.set(true);
    const fv = this.userForm.getRawValue();
 
    if (editing) {
      // Update name/phone
      const updateObs$ = this.auth.isSuperAdmin()
        ? this.userSvc.updateUser(editing.id, { fname: fv.fname!, lname: fv.lname!, phoneNumber: fv.phoneNumber || undefined })
        : this.userSvc.updateUser(editing.id, { fname: fv.fname!, lname: fv.lname!, phoneNumber: fv.phoneNumber || undefined });
 
      updateObs$.subscribe({
        next: saved => {
          // Also update role if super admin changed it
          if (this.auth.isSuperAdmin() && fv.role && fv.role !== editing.role) {
            this.userSvc.updateRole(editing.id, { role: fv.role as UserRole }).subscribe({
              next: withRole => {
                this.users.update(l => l.map(u => u.id === withRole.id ? withRole : u));
                this.toast.success('User updated.');
                this.closeModal();
                this.saving.set(false);
              },
              error: () => this.saving.set(false)
            });
          } else {
            this.users.update(l => l.map(u => u.id === saved.id ? saved : u));
            this.toast.success('User updated.');
            this.closeModal();
            this.saving.set(false);
          }
        },
        error: () => this.saving.set(false)
      });
    } else {
      const req: CreateUserRequest = {
        fname: fv.fname!, lname: fv.lname!, email: fv.email!,
        password: fv.password!, phoneNumber: fv.phoneNumber || undefined,
        role: (fv.role as UserRole) || 'EMPLOYEE'
      };
 
      const createObs$ = this.auth.isSuperAdmin()
        ? this.userSvc.createUser(req)
        : this.userSvc.createEmployee(req);
 
      createObs$.subscribe({
        next: saved => {
          this.users.update(l => [saved, ...l]);
          this.toast.success('User created. Welcome email sent!');
          this.closeModal();
          this.saving.set(false);
        },
        error: () => this.saving.set(false)
      });
    }
  }
 
  activate(user: UserResponse): void {
    const obs$ = this.auth.isSuperAdmin()
      ? this.userSvc.activateUser(user.id)
      : this.userSvc.activateEmployee(user.id);
 
    obs$.subscribe({
      next: () => {
        this.users.update(l => l.map(u => u.id === user.id ? { ...u, isActive: true } : u));
        this.toast.success(`${user.fname} activated.`);
      }
    });
  }
 
  deactivate(user: UserResponse): void {
    if (!confirm(`Deactivate ${user.fname}?`)) return;
    const obs$ = this.auth.isSuperAdmin()
      ? this.userSvc.deactivateUser(user.id)
      : this.userSvc.deactivateEmployee(user.id);
 
    obs$.subscribe({
      next: () => {
        this.users.update(l => l.map(u => u.id === user.id ? { ...u, isActive: false } : u));
        this.toast.warning(`${user.fname} deactivated.`);
      }
    });
  }
 
  confirmDelete(user: UserResponse): void { this.deleteTarget.set(user); }
 
  deleteUser(): void {
    const user = this.deleteTarget();
    if (!user) return;
    this.saving.set(true);
 
    const obs$ = this.auth.isSuperAdmin()
      ? this.userSvc.deleteUser(user.id)
      : this.userSvc.deleteEmployee(user.id);
 
    obs$.subscribe({
      next: () => {
        this.users.update(l => l.filter(u => u.id !== user.id));
        this.toast.success('User deleted.');
        this.deleteTarget.set(null);
        this.saving.set(false);
      },
      error: () => this.saving.set(false)
    });
  }
}
// Make "getUserById(id: number): Observable<UserResponse>" , "getUsersByRole(role: UserRole): Observable<UserResponse[]>"
// & "getEmployeeById(id : number): Observable<UserResponse[]>" 