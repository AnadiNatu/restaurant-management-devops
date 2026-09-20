import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivityLog } from '../../core/models/model';
import { ActivityLogService } from '../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-activity-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activity-logs.component.html',
  styleUrl: './activity-logs.component.css'
})
export class ActivityLogsComponent implements OnInit {
 private logSvc = inject(ActivityLogService);
 
  loading      = signal(true);
  logs         = signal<ActivityLog[]>([]);
  searchQuery  = signal('');
  entityFilter = signal('');
  roleFilter   = signal('');

  actorEmail = signal('');
  fromDate = signal('');
  toDate = signal('');
 
  entityTypes = computed(() =>
    [...new Set(this.logs().map(l => l.entityType).filter(Boolean) as string[])].sort()
  );
 
  filtered = computed(() => {
    let list = this.logs();
    const q = this.searchQuery().toLowerCase();
    if (q) list = list.filter(l =>
      l.actorEmail.toLowerCase().includes(q) ||
      l.action.toLowerCase().includes(q) ||
      l.description?.toLowerCase().includes(q) ||
      l.entityType?.toLowerCase().includes(q)
    );
    if (this.entityFilter()) list = list.filter(l => l.entityType === this.entityFilter());
    if (this.roleFilter())   list = list.filter(l => l.actorRole  === this.roleFilter());
    return list;
  });
 
  /** Converts ACTION_NAME → Action name (no regex in template) */
  formatAction(action: string): string {
    return action.split('_').join(' ');
  }
 
  /** Converts ACTOR_ROLE → Actor role */
  formatRole(role: string): string {
    return role.split('_').join(' ');
  }
 
  roleClass(role: string): string {
    const map: Record<string, string> = {
      SUPER_ADMIN: 'badge-super',
      ADMIN:       'badge-amber',
      EMPLOYEE:    'badge-info'
    };
    return map[role] ?? 'badge-info';
  }
 
  ngOnInit(): void { this.load(); }
 
  load(): void {
    this.loading.set(true);
    this.logSvc.getAll().subscribe({
      next:  logs => { this.logs.set(logs); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

loadByActor(): void {
    const email = this.actorEmail().trim();

    if (!email) {
      this.load();
      return;
    }

    this.loading.set(true);

    this.logSvc.getByActor(email).subscribe({
      next: logs => {
        this.logs.set(logs);
        this.loading.set(false);
      },

      error: () => {
        this.loading.set(false);
      }
    });
  }

   loadByEntityType(): void {
    const type = this.entityFilter().trim();

    if (!type) {
      this.load();
      return;
    }

    this.loading.set(true);

    this.logSvc.getByEntityType(type).subscribe({
      next: logs => {
        this.logs.set(logs);
        this.loading.set(false);
      },

      error: () => {
        this.loading.set(false);
      }
    });
  }

  loadBetween(): void {
    const from = this.fromDate();
    const to = this.toDate();

    if (!from || !to) {
      return;
    }

    this.loading.set(true);

    this.logSvc.getBetween(from, to).subscribe({
      next: logs => {
        this.logs.set(logs);
        this.loading.set(false);
      },

      error: () => {
        this.loading.set(false);
      }
    });
  }

  clearFilters(): void {
    this.actorEmail.set('');
    this.entityFilter.set('');
    this.roleFilter.set('');
    this.searchQuery.set('');
    this.fromDate.set('');
    this.toDate.set('');

    this.load();
  }
// Make use of the getByActor(email: string): Observable<ActivityLog[]>
// & getByEntityType(type: string): Observable<ActivityLog[]> 
// & getBetween(from: string, to: string): Observable<ActivityLog[]>
}
