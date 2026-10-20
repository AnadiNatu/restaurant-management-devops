import { Component, inject, OnInit, signal } from '@angular/core';
import { RevenueSummary, CostSummaryResponse } from '../../core/models/model';
import { ReportService, InventoryService } from '../../core/services/api.service';
import { CommonModule } from '@angular/common';
import { forkJoin, Observable } from 'rxjs';

type ReportPeriod = 'weekly' | 'monthly';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
private readonly reportSvc = inject(ReportService);
private invSvc    = inject(InventoryService);
 
  loading = signal(true);
  downloading = signal<boolean>(false);
  period  = signal<ReportPeriod>('weekly');
  revenue = signal<RevenueSummary | null>(null);
  invCost = signal<CostSummaryResponse | null>(null);
 
  errorMessage = signal<string | null>(null);
  downloadMessage = signal<string | null>(null);

  fromDate = signal<string>('');
  toDate = signal<string>('');

  ngOnInit(): void { this.fetch(); }

  setPeriod(period: ReportPeriod): void {
    if (this.period() === period) {
      return;
    }

    this.period.set(period);
    this.fetch();
  }

  avgRevenue(): number {
    const r = this.revenue();
    if (!r || r.totalOrders === 0) return 0;
    return r.totalRevenue / r.totalOrders;
  }
  
 
  // revenueExclTaxPct(): number {
  //   const r = this.revenue();
  //   if (!r || r.totalRevenue === 0) return 100;
  //   return Math.min(100, ((r.totalRevenue - (r.totalTax ?? 0)) / r.totalRevenue) * 100);
  // }
 
  // taxPct(): number {
  //   const r = this.revenue();
  //   if (!r || r.totalRevenue === 0) return 0;
  //   return Math.min(100, ((r.totalTax ?? 0) / r.totalRevenue) * 100);
  // }
 
  // grossMarginPct(): number {
  //   const r = this.revenue();
  //   const c = this.invCost();
  //   if (!r || !c || r.totalRevenue === 0) return 0;
  //   return ((r.totalRevenue - c.totalCost) / r.totalRevenue) * 100;
  // }
 
 fetch(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const isWeekly = this.period() === 'weekly';

    const revenueRequest = isWeekly
      ? this.reportSvc.getWeeklyRevenue()
      : this.reportSvc.getMonthlyRevenue();

    const inventoryRequest = isWeekly
      ? this.reportSvc.getWeeklyInventoryCost()
      : this.reportSvc.getMonthlyInventoryCost();

    forkJoin({
      revenue: revenueRequest,
      inventory: inventoryRequest
    }).subscribe({
      next: ({ revenue, inventory }) => {
        this.revenue.set(revenue);
        this.invCost.set(inventory);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        console.error('Failed to load reports:', error);
        this.errorMessage.set(
          'Unable to load the report data. Please try again.'
        );
        this.loading.set(false);
      }
    });
  }

  // ---------- Revenue values ----------

  grossSales(): number {
    return this.toNumber(this.revenue()?.grossSales);
  }

  totalDiscount(): number {
    return this.toNumber(this.revenue()?.totalDiscount);
  }

  taxableSales(): number {
    const report = this.revenue();

    if (!report) {
      return 0;
    }

    return this.toNumber(report.taxableSales ?? report.totalRevenue);
  }

  totalRevenue(): number {
    return this.toNumber(this.revenue()?.totalRevenue);
  }

  totalTax(): number {
    return this.toNumber(this.revenue()?.totalTax);
  }

  totalInvoiceValue(): number {
    const report = this.revenue();

    if (!report) {
      return 0;
    }

    // Fallback keeps the UI usable if an older API response omits this field.
    return this.toNumber(
      report.totalInvoiceValue ?? (report.totalRevenue + (report.totalTax ?? 0))
    );
  }

  totalOrders(): number {
    return this.toNumber(this.revenue()?.totalOrders);
  }

  averageInvoiceValue(): number {
    const count = this.totalOrders();

    if (count <= 0) {
      return 0;
    }

    return this.totalInvoiceValue() / count;
  }

  totalCgst(): number {
    return this.toNumber(this.revenue()?.totalCgst);
  }

  totalSgst(): number {
    return this.toNumber(this.revenue()?.totalSgst);
  }

  totalIgst(): number {
    return this.toNumber(this.revenue()?.totalIgst);
  }

  // ---------- Revenue breakdown percentages ----------

  revenueExclTaxPct(): number {
    const invoiceValue = this.totalInvoiceValue();

    if (invoiceValue <= 0) {
      return 0;
    }

    return this.percentage(this.taxableSales(), invoiceValue);
  }

  taxPct(): number {
    const invoiceValue = this.totalInvoiceValue();

    if (invoiceValue <= 0) {
      return 0;
    }

    return this.percentage(this.totalTax(), invoiceValue);
  }

  discountPct(): number {
    const gross = this.grossSales();

    if (gross <= 0) {
      return 0;
    }

    return this.percentage(this.totalDiscount(), gross);
  }

  // ---------- Inventory and margin ----------

  inventoryTotalCost(): number {
    return this.toNumber(this.invCost()?.totalCost);
  }

  inventoryTotalItems(): number {
    return this.toNumber(this.invCost()?.totalItems);
  }

  lowStockCount(): number {
    return this.toNumber(this.invCost()?.lowStockCount);
  }

  grossMarginAmount(): number {
    return this.totalRevenue() - this.inventoryTotalCost();
  }

  grossMarginPct(): number {
    const revenue = this.totalRevenue();

    if (revenue <= 0) {
      return 0;
    }

    return (this.grossMarginAmount() / revenue) * 100;
  }

  revenueBarFlex(): number {
    return Math.max(this.totalRevenue(), 0.01);
  }

  costBarFlex(): number {
    return Math.max(this.inventoryTotalCost(), 0.01);
  }

  // ---------- PDF downloads ----------

  downloadPeriodPdf(): void {
    this.downloadMessage.set(null);
    this.errorMessage.set(null);
    this.downloading.set(true);

    const isWeekly = this.period() === 'weekly';

    const request = isWeekly
      ? this.reportSvc.getWeeklyRevenuePdf()
      : this.reportSvc.getMonthlyRevenuePdf();

    const filename = isWeekly
      ? 'weekly-revenue-report.pdf'
      : 'monthly-revenue-report.pdf';

    this.downloadBlob(request, filename);
  }

  downloadCustomPdf(): void {
    const from = this.fromDate();
    const to = this.toDate();

    this.downloadMessage.set(null);
    this.errorMessage.set(null);

    if (!from || !to) {
      this.errorMessage.set('Please select both the start and end dates.');
      return;
    }

    if (from > to) {
      this.errorMessage.set(
        'The start date must be before or equal to the end date.'
      );
      return;
    }

    /*
     * The backend accepts LocalDateTime query parameters.
     * Use the start of the first day and the end of the selected final day.
     */
    const fromDateTime = `${from}T00:00:00`;
    const toDateTime = `${to}T23:59:59`;

    this.downloading.set(true);

    this.downloadBlob(
      this.reportSvc.getDownloadRevenuePdf(fromDateTime, toDateTime),
      `revenue-report-${from}-to-${to}.pdf`
    );
  }

  private downloadBlob(
    request: Observable<Blob>,
    filename: string
  ): void {
    request.subscribe({
      next: (blob: Blob) => {
        if (!blob || blob.size === 0) {
          this.errorMessage.set(
            'The server returned an empty PDF. Please try again.'
          );
          this.downloading.set(false);
          return;
        }

        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');

        anchor.href = url;
        anchor.download = filename;
        anchor.style.display = 'none';

        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();

        URL.revokeObjectURL(url);

        this.downloadMessage.set('Your PDF download has started.');
        this.downloading.set(false);
      },
      error: (error: unknown) => {
        console.error('Failed to download PDF:', error);
        this.errorMessage.set(
          'Unable to download the PDF. Please check the server and try again.'
        );
        this.downloading.set(false);
      }
    });
  }

  // ---------- Formatting helpers ----------

  private toNumber(value: unknown): number {
    if (value === null || value === undefined || value === '') {
      return 0;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  private percentage(part: number, total: number): number {
    if (total <= 0) {
      return 0;
    }

    return Math.max(0, Math.min(100, (part / total) * 100));
  }
}
