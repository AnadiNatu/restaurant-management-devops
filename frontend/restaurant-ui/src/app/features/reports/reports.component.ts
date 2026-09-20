import { Component, inject, OnInit, signal } from '@angular/core';
import { RevenueSummary, CostSummaryResponse } from '../../core/models/model';
import { ReportService, InventoryService } from '../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
private reportSvc = inject(ReportService);
  private invSvc    = inject(InventoryService);
 
  loading = signal(true);
  period  = signal<'weekly' | 'monthly'>('weekly');
  revenue = signal<RevenueSummary | null>(null);
  invCost = signal<CostSummaryResponse | null>(null);
 
  avgRevenue(): number {
    const r = this.revenue();
    if (!r || r.totalOrders === 0) return 0;
    return r.totalRevenue / r.totalOrders;
  }
 
  revenueExclTaxPct(): number {
    const r = this.revenue();
    if (!r || r.totalRevenue === 0) return 100;
    return Math.min(100, ((r.totalRevenue - (r.totalTax ?? 0)) / r.totalRevenue) * 100);
  }
 
  taxPct(): number {
    const r = this.revenue();
    if (!r || r.totalRevenue === 0) return 0;
    return Math.min(100, ((r.totalTax ?? 0) / r.totalRevenue) * 100);
  }
 
  grossMarginPct(): number {
    const r = this.revenue();
    const c = this.invCost();
    if (!r || !c || r.totalRevenue === 0) return 0;
    return ((r.totalRevenue - c.totalCost) / r.totalRevenue) * 100;
  }
 
  ngOnInit(): void { this.fetch(); }
 
  setPeriod(p: 'weekly' | 'monthly'): void {
    this.period.set(p);
    this.fetch();
  }
 
  fetch(): void {
    this.loading.set(true);
    const isWeekly = this.period() === 'weekly';
 
    const rev$ = isWeekly ? this.reportSvc.getWeeklyRevenue()  : this.reportSvc.getMonthlyRevenue();
    // const inv$ = isWeekly ? this.invSvc.getWeeklyCost() 
    const inv$ = isWeekly
      ? this.reportSvc.getWeeklyInventoryCost()
      : this.reportSvc.getMonthlyInventoryCost();
//  Need to add getWeeklyInventoryCost(): Observable<InventoryCostSummary> & getMonthlyInventoryCost(): Observable<InventoryCostSummary>
    rev$.subscribe({
      next: r => {
        this.revenue.set(r);
        inv$.subscribe({
          next:  c => { this.invCost.set(c); this.loading.set(false); },
          error: () => this.loading.set(false)
        });
      },
      error: () => this.loading.set(false)
    });
  }
}
