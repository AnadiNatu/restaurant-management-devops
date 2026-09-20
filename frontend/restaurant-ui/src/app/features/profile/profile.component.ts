import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserResponse } from '../../core/models/model';
import { ProfileService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
 private profileSvc = inject(ProfileService);
  private authSvc    = inject(AuthService);
  private toast      = inject(ToastService);
  private fb         = inject(FormBuilder);
 
  loading         = signal(true);
  savingProfile   = signal(false);
  savingPassword  = signal(false);
  profile         = signal<UserResponse | null>(null);
 
  profileForm = this.fb.group({
    fname:       ['', Validators.required],
    lname:       ['', Validators.required],
    phoneNumber: ['']
  });
 
  passwordForm = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword:     ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  });
 
  initials(): string {
    const p = this.profile();
    if (!p) return '?';
    return `${p.fname[0] ?? ''}${p.lname[0] ?? ''}`.toUpperCase();
  }
 
  roleClass(role: string): string {
    const map: Record<string, string> = {
      SUPER_ADMIN: 'badge-super',
      ADMIN:       'badge-amber',
      EMPLOYEE:    'badge-info'
    };
    return map[role] ?? 'badge-info';
  }
 
  passwordMismatch(): boolean {
    const f = this.passwordForm;
    return f.controls.newPassword.valid &&
           f.controls.confirmPassword.touched &&
           f.value.newPassword !== f.value.confirmPassword;
  }
 
  ngOnInit(): void {
    this.profileSvc.getProfile().subscribe({
      next: p => {
        this.profile.set(p);
        this.profileForm.patchValue({
          fname: p.fname, lname: p.lname, phoneNumber: p.phoneNumber ?? ''
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
 
  saveProfile(): void {
    if (this.profileForm.invalid) { this.profileForm.markAllAsTouched(); return; }
    this.savingProfile.set(true);
    const val = this.profileForm.getRawValue();
 
    this.profileSvc.updateProfile({
      fname: val.fname!, lname: val.lname!, phoneNumber: val.phoneNumber || undefined
    }).subscribe({
      next: updated => {
        this.profile.set(updated);
        this.toast.success('Profile updated.');
        this.savingProfile.set(false);
      },
      error: () => this.savingProfile.set(false)
    });
  }
 
  changePassword(): void {
    if (this.passwordForm.invalid || this.passwordMismatch()) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.savingPassword.set(true);
    const val = this.passwordForm.getRawValue();
 
    this.authSvc.changePassword({
      currentPassword: val.currentPassword!,
      newPassword:     val.newPassword!
    }).subscribe({
      next: () => {
        this.toast.success('Password changed successfully.');
        this.passwordForm.reset();
        this.savingPassword.set(false);
      },
      error: err => {
        this.toast.error(err?.error?.message || 'Failed to change password.');
        this.savingPassword.set(false);
      }
    });
  }
}
