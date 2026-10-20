import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { CommonModule } from '@angular/common';
import { AuthResponse } from '../../../core/models/model';
interface DemoAccount {
  role: string;
  description: string;
  email: string;
  password: string;
  icon: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CommonModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  loading = signal(false);
  showPass = signal(false);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  // Public demo accounts for website reviewers.
  readonly demoAccounts: DemoAccount[] = [
    {
      role: 'Super Admin',
      description: 'Full system administration',
      email: 'anadinatu+superadmin@gmail.com',
      password: 'password1234',
      icon: '👑'
    },
    {
      role: 'Admin',
      description: 'Restaurant management',
      email: 'anadinatu+admin@gmail.com',
      password: 'password123',
      icon: '🛡️'
    },
    {
      role: 'Employee 1',
      description: 'Employee account',
      email: 'anadinatu+employee1@gmail.com',
      password: 'password123',
      icon: '👨‍🍳'
    },
    {
      role: 'Employee 2',
      description: 'Employee account',
      email: 'anadinatu+employee2@gmail.com',
      password: 'password123',
      icon: '🧑‍💼'
    }
  ];

  get email() {
    return this.form.controls.email;
  }

  get password() {
    return this.form.controls.password;
  }

  /**
   * Select a demo account, fill the login form,
   * and immediately submit the login request.
   */
  loginWithDemo(account: DemoAccount): void {
    if (this.loading()) {
      return;
    }

    this.form.setValue({
      email: account.email,
      password: account.password
    });

    this.form.markAsPristine();
    this.form.markAsUntouched();

    this.submit();
  }

  submit(): void {
    if (this.loading()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);

    this.auth.login(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.toast.success('Welcome back!');
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.loading.set(false);
        this.toast.error(
          err?.error?.message || 'Invalid email or password.'
        );
      },
      complete: () => {
        this.loading.set(false);
      }
    });
  }
}