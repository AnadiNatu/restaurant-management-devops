import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { CommonModule } from '@angular/common';
import { AuthResponse } from '../../../core/models/model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule , RouterLink , CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
private fb    = inject(FormBuilder);
  private auth  = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);
 
  loading  = signal(false);
  showPass = signal(false);
 
  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });
 
  get email()    { return this.form.controls.email; }
  get password() { return this.form.controls.password; }
 
  submit(): void {
    console.log('🔥🔥🔥 SIGN IN BUTTON HANDLER FIRED 🔥🔥🔥');
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
 
    this.auth.login(this.form.getRawValue() as any).subscribe({
      next: (response) => {
        console.log('🔥 LOGIN SUCCESS:', response);
        this.toast.success('Welcome back!');
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.loading.set(false);
        this.toast.error(err?.error?.message || 'Invalid email or password.');
      },
      complete: () => this.loading.set(false)
    });
  }
}
