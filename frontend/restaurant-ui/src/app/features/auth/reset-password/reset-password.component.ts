import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule , RouterLink , CommonModule],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent {
 private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private toast  = inject(ToastService);
  private router = inject(Router);
 
  loading = signal(false);
 
  form = this.fb.group({
    token:       ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]]
  });
 
  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
 
    this.auth.resetPassword(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.toast.success('Password reset successfully. Please log in.');
        this.router.navigate(['/auth/login']);
      },
      error: err => {
        this.loading.set(false);
        this.toast.error(err?.error?.message || 'Reset failed. Check your token.');
      }
    });
  }
}
