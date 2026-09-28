import { ChangeDetectionStrategy, Component, OnInit, computed, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import type { ChartData, ChartOptions, TooltipItem } from 'chart.js';

import { DashboardService } from '../../core/services/dashboard.service';
import { PageHeader } from '../../shared/page-header';
import { StatCard } from '../../shared/stat-card';
import { EmptyState, Loading } from '../../shared/states';
import { formatDate, formatMoney } from '../../core/utils/format';

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DecimalPipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    RouterLink,
    BaseChartDirective,
    PageHeader,
    StatCard,
    EmptyState,
    Loading,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardPage implements OnInit {
  private readonly dashboard = inject(DashboardService);

  readonly loading = this.dashboard.loading;
  readonly error = this.dashboard.error;
  readonly snapshot = this.dashboard.snapshot;
  readonly periodLabel = this.dashboard.periodLabel;
  readonly period = this.dashboard.period;

  readonly formatMoney = formatMoney;
  readonly formatDate = formatDate;

  /** Meses ofrecidos en el selector de período. */
  readonly months = buildMonths(12);

  // -------------------------------------------------------------------------
  // Series de datos
  // -------------------------------------------------------------------------

  /** Gráfico 1: evolución diaria de la asistencia. */
  readonly dailyData = computed<ChartData<'bar' | 'line'>>(() => {
    const daily = this.snapshot()?.daily ?? [];
    return {
      labels: daily.map((d) => d.label),
      datasets: [
        {
          type: 'line' as const,
          label: '% asistencia',
          data: daily.map((d) => Number(d.porcentaje.toFixed(1))),
          borderColor: '#1565c0',
          backgroundColor: 'rgba(21, 101, 192, 0.12)',
          fill: true,
          tension: 0.35,
          pointRadius: 2,
          pointHoverRadius: 5,
          borderWidth: 2,
          yAxisID: 'y',
          order: 0,
        },
        {
          type: 'bar' as const,
          label: 'Presentes',
          data: daily.map((d) => d.presentes),
          backgroundColor: 'rgba(21, 101, 192, 0.75)',
          borderRadius: 6,
          yAxisID: 'y1',
          order: 1,
        },
        {
          type: 'bar' as const,
          label: 'Inasistencias',
          data: daily.map((d) => d.inasistencias),
          backgroundColor: 'rgba(198, 40, 40, 0.8)',
          borderRadius: 6,
          yAxisID: 'y1',
          order: 1,
        },
      ],
    };
  });

  readonly dailyOptions: ChartOptions<'bar' | 'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    scales: {
      y: {
        position: 'left',
        min: 0,
        max: 100,
        title: { display: true, text: '% asistencia' },
        grid: { color: 'rgba(0,0,0,0.06)' },
      },
      y1: {
        position: 'right',
        beginAtZero: true,
        title: { display: true, text: 'Días' },
        grid: { drawOnChartArea: false },
      },
      x: { grid: { display: false } },
    },
    plugins: {
      legend: { position: 'bottom' },
      tooltip: {
        callbacks: {
          label: (item: TooltipItem<'bar' | 'line'>) => {
            const v = Number(item.raw ?? 0);
            return item.dataset.yAxisID === 'y' ? `${item.dataset.label}: ${v}%` : `${item.dataset.label}: ${v}`;
          },
        },
      },
    },
  };

  /** Gráfico 2: reparto de estados del período. */
  readonly splitData = computed<ChartData<'doughnut'>>(() => {
    const s = this.snapshot()?.split ?? { presentes: 0, inasistencias: 0, licencias: 0 };
    return {
      labels: ['Presentes', 'Inasistencias', 'Licencias médicas'],
      datasets: [
        {
          data: [s.presentes, s.inasistencias, s.licencias],
          backgroundColor: ['#2e7d32', '#c62828', '#f9a825'],
          borderWidth: 0,
          hoverOffset: 8,
        },
      ],
    };
  });

  readonly splitOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '62%',
    plugins: {
      legend: { position: 'bottom' },
    },
  };

  /** Gráfico 3: porcentaje de asistencia por empleado (barras verticales). */
  readonly porEmpleadoData = computed<ChartData<'bar'>>(() => {
    const rows = this.snapshot()?.porEmpleado ?? [];
    return {
      // Solo el nombre: en vertical el eje X es angosto y el nombre completo
      // se cortaría. El apellido y el RUT se ven igual en la tabla de detalle.
      labels: rows.map((r) => r.employee.firstName),
      datasets: [
        {
          label: '% asistencia',
          data: rows.map((r) => Number(r.porcentaje.toFixed(1))),
          backgroundColor: rows.map((r) => colorForPercentage(r.porcentaje)),
          borderRadius: 8,
          maxBarThickness: 64,
        },
      ],
    };
  });

  readonly porEmpleadoOptions: ChartOptions<'bar'> = {
    // Barras verticales: una por empleado, con el porcentaje en el eje Y.
    indexAxis: 'x',
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        min: 0,
        max: 100,
        title: { display: true, text: '% asistencia' },
        grid: { color: 'rgba(0,0,0,0.06)' },
      },
      x: {
        grid: { display: false },
        ticks: { autoSkip: false, maxRotation: 0, minRotation: 0 },
      },
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (item: TooltipItem<'bar'>) => `${item.raw}% de asistencia`,
        },
      },
    },
  };

  /**
   * Alto del gráfico según la cantidad de empleados: en vertical, una barra
   * por persona necesita una caja más alta para que se lean bien.
   */
  readonly altoGraficoEmpleados = computed(() => {
    const total = this.snapshot()?.porEmpleado?.length ?? 0;
    if (total === 0) return 220;
    return Math.min(240 + total * 8, 320);
  });

  /** Fuente de datos de la tabla de detalle por empleado. */
  readonly detailColumns = ['empleado', 'presentes', 'inasistencias', 'licencias', 'porcentaje'];

  readonly hasData = computed(() => (this.snapshot()?.totalEmpleados ?? 0) > 0);

  ngOnInit(): void {
    this.dashboard.load();
  }

  onPeriodChange(value: string): void {
    const [year, month] = value.split('-').map((n) => Number(n));
    this.dashboard.load(year, month);
  }

  /** Clave `YYYY-MM` del período actual; debe coincidir con `buildMonths`. */
  periodValue(): string {
    const { year, month } = this.period();
    return `${year}-${String(month).padStart(2, '0')}`;
  }

  reload(): void {
    this.dashboard.load(this.period().year, this.period().month);
  }
}

/** Verde si va bien, ámbar si es regular, rojo si es crítico. */
function colorForPercentage(pct: number): string {
  if (pct >= 90) return 'rgba(46, 125, 50, 0.85)';
  if (pct >= 75) return 'rgba(249, 168, 37, 0.85)';
  return 'rgba(198, 40, 40, 0.85)';
}

/** Genera los últimos `count` meses hacia atrás, incluido el actual. */
function buildMonths(count: number): { value: string; label: string }[] {
  const out: { value: string; label: string }[] = [];
  const now = new Date();
  for (let i = count - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const year = d.getFullYear();
    const month = d.getMonth() + 1;
    out.push({
      value: `${year}-${String(month).padStart(2, '0')}`,
      label: d.toLocaleDateString('es-CL', { month: 'long', year: 'numeric' }),
    });
  }
  return out;
}
