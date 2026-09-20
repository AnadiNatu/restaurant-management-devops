import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule , RouterLink , CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private toast  = inject(ToastService);
  private router = inject(Router);
 
  loading = signal(false);
 
  form = this.fb.group({
    fname:       ['', Validators.required],
    lname:       ['', Validators.required],
    email:       ['', [Validators.required, Validators.email]],
    password:    ['', [Validators.required, Validators.minLength(8)]],
    phoneNumber: ['']
  });
 
  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
 
    this.auth.register(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.toast.success('Account created! Welcome to Bistro Hub.');
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.loading.set(false);
        this.toast.error(err?.error?.message || 'Registration failed. Try again.');
      },
      complete: () => this.loading.set(false)
    });
  }
}
