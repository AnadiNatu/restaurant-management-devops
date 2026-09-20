import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
private fb    = inject(FormBuilder);
  private auth  = inject(AuthService);
  private toast = inject(ToastService);
 
  loading = signal(false);
  sent    = signal(false);
 
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });
 
  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
 
    this.auth.forgotPassword({ email: this.form.value.email! }).subscribe({
      next: () => { this.sent.set(true); this.loading.set(false); },
      error: err => {
        this.loading.set(false);
        this.toast.error(err?.error?.message || 'Failed to send reset link.');
      }
    });
  }
}
