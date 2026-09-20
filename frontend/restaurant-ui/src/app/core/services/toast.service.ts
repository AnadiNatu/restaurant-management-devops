import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  message: string;
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  private add(type: ToastType, message: string, duration = 4000): void {
    const id = Math.random().toString(36).slice(2);
    this.toasts.update(t => [...t, { id, type, message, duration }]);
    setTimeout(() => this.remove(id), duration);
  }

  success(message: string, duration?: number): void { this.add('success', message, duration); }
  error(message: string, duration?: number): void   { this.add('error', message, duration ?? 6000); }
  warning(message: string, duration?: number): void { this.add('warning', message, duration); }
  info(message: string, duration?: number): void    { this.add('info', message, duration); }

  remove(id: string): void {
    this.toasts.update(t => t.filter(x => x.id !== id));
  }
}