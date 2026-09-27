import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin, firstValueFrom, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { map } from 'rxjs/operators';

import {
  AdvanceRequest,
  AdvanceResponse,
  EmployeeResponse,
  HoraExtraRequest,
  HoraExtraResponse,
  LicenciaRequest,
  LicenciaResponse,
  LiquidacionResponse,
} from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import { formatDate, formatMoney, monthLabel, normalizeText } from '../../core/utils/format';
import { PageHeader } from '../../shared/page-header';
import { ConfirmDialog, ConfirmData } from '../../shared/confirm-dialog';
import { EmptyState, Loading } from '../../shared/states';
import { AdvanceDialog, AdvanceDialogData } from './advance-dialog';
import { HoraExtraDialog, HoraExtraDialogData } from './hora-extra-dialog';
import { LicenciaDialog, LicenciaDialogData } from './licencia-dialog';
import { LiquidacionDetailDialog, LiquidacionDetailData } from './liquidacion-detail-dialog';

@Component({
  selector: 'app-payroll',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTabsModule,
    MatTooltipModule,
    PageHeader,
    EmptyState,
    Loading,
  ],
  templateUrl: './payroll.html',
  styleUrl: './payroll.scss',
})
export class PayrollPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly employees = signal<EmployeeResponse[]>([]);
  readonly liquidaciones = signal<Record<number, LiquidacionResponse[]>>({});
  readonly advances = signal<AdvanceResponse[]>([]);
  readonly licencias = signal<LicenciaResponse[]>([]);
  readonly horasExtras = signal<Record<number, HoraExtraResponse[]>>({});

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');

  /** Período de visualización de la pestaña Liquidaciones. */
  readonly visYear = signal(new Date().getFullYear());
  readonly visMonth = signal(new Date().getMonth() + 1);

  /** Período a liquidar. */
  readonly genYear = signal(new Date().getFullYear());
  readonly genMonth = signal(new Date().getMonth() + 1);

  readonly search = signal('');
  readonly tab = signal<number>(0);

  readonly formatDate = formatDate;
  readonly formatMoney = formatMoney;
  readonly monthLabel = monthLabel;

  readonly months = Array.from({ length: 12 }, (_, i) => i + 1);
  readonly years = computed(() => {
    const base = new Date().getFullYear();
    return Array.from({ length: 6 }, (_, i) => base - 2 + i);
  });
  readonly monthNames = computed(() =>
    this.months.map((m) => ({ value: m, label: monthLabel(this.visYear(), m) })),
  );

  readonly liquidacionColumns = [
    'empleado',
    'periodo',
    'sueldoBase',
    'liquido',
    'estado',
    'acciones',
  ];
  readonly extrasColumns = ['fecha', 'horas', 'monto', 'acciones'];
  readonly advancesColumns = ['fecha', 'descripcion', 'monto', 'acciones'];
  readonly licenciasColumns = ['inicio', 'fin', 'dias', 'motivo', 'acciones'];

  /** Empleados habilitados, filtrados por la búsqueda. */
  readonly empleadosFiltrados = computed(() => {
    const q = normalizeText(this.search());
    return this.employees().filter(
      (e) => e.enabled && (!q || normalizeText(e.fullName).includes(q) || normalizeText(e.rut).includes(q)),
    );
  });

  readonly deshabilitados = computed(() => this.employees().filter((e) => !e.enabled).length);

  readonly totalLiquidaciones = computed(() =>
    Object.values(this.liquidaciones()).reduce((sum, list) => sum + list.length, 0),
  );

  ngOnInit(): void {
    void this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      const employees = await firstValueFrom(this.api.listEmployees());
      this.employees.set(employees);

      // Cada endpoint por empleado falla de forma independiente; un fallo no
      // debe dejar la página entera sin datos.
      const { liqs, advs, lics, extras } = await firstValueFrom(
        forkJoin({
          liqs: forkJoin(
            employees.map((e) =>
              this.api
                .listLiquidaciones(e.id)
                .pipe(catchError(() => of([] as LiquidacionResponse[]))),
            ),
          ),
          advs: forkJoin(
            employees.map((e) =>
              this.api.listAdvances(e.id).pipe(catchError(() => of([] as AdvanceResponse[]))),
            ),
          ),
          lics: forkJoin(
            employees.map((e) =>
              this.api.listLicencias(e.id).pipe(catchError(() => of([] as LicenciaResponse[]))),
            ),
          ),
          extras: forkJoin(
            employees.map((e) =>
              this.api.listHorasExtras(e.id).pipe(catchError(() => of([] as HoraExtraResponse[]))),
            ),
          ),
        }),
      );

      const byEmployee = <T>(list: T[][]): Record<number, T[]> => {
        const map: Record<number, T[]> = {};
        employees.forEach((e, i) => (map[e.id] = list[i] ?? []));
        return map;
      };

      this.liquidaciones.set(byEmployee(liqs));
      this.advances.set(advs.flat());
      this.licencias.set(lics.flat());
      this.horasExtras.set(byEmployee(extras));
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudieron cargar los datos de remuneraciones'));
    } finally {
      this.loading.set(false);
    }
  }

  onSearchChange(value: string): void {
    this.search.set(value);
  }

  onVisMonthChange(value: number): void {
    this.visMonth.set(value);
  }

  onVisYearChange(value: number): void {
    this.visYear.set(value);
  }

  onGenMonthChange(value: number): void {
    this.genMonth.set(value);
  }

  onGenYearChange(value: number): void {
    this.genYear.set(value);
  }

  /** Liquidación del empleado para el período visible, si existe. */
  liquidacionDe(employeeId: number): LiquidacionResponse | null {
    return (
      this.liquidaciones()[employeeId]?.find(
        (l) => l.periodMonth === this.visMonth() && l.periodYear === this.visYear(),
      ) ?? null
    );
  }

  extrasDe(employeeId: number): HoraExtraResponse[] {
    return this.horasExtras()[employeeId] ?? [];
  }

  anticiposDe(employeeId: number): AdvanceResponse[] {
    return this.advances().filter((a) => a.employeeId === employeeId);
  }

  licenciasDe(employeeId: number): LicenciaResponse[] {
    return this.licencias().filter((l) => l.employeeId === employeeId);
  }

  /** Suma de los montos de horas extra del empleado en el período visible. */
  totalExtrasPeriodo(employeeId: number): number {
    return this.extrasDe(employeeId)
      .filter((h) => this.enPeriodo(h.date))
      .reduce((sum, h) => sum + (h.monto ?? 0), 0);
  }

  private enPeriodo(iso: string): boolean {
    const [y, m] = iso.split('-').map(Number);
    return y === this.visYear() && m === this.visMonth();
  }

  async generarTodas(): Promise<void> {
    const data: ConfirmData = {
      title: 'Generar liquidaciones',
      message: `¿Genera la liquidación de ${this.employees().filter((e) => e.enabled).length} empleados para ${
        monthLabel(this.genYear(), this.genMonth())
      }? Las liquidaciones existentes del período serán reemplazadas.`,
      confirmText: 'Generar',
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    this.saving.set(true);
    this.error.set('');
    try {
      const res = await firstValueFrom(this.api.liquidarTodos(this.genYear(), this.genMonth()));
      const count = res.count ?? 0;
      this.snackBar.open(
        count === 1 ? 'Se creó 1 liquidación' : `Se crearon ${count} liquidaciones`,
        'Cerrar',
        { duration: 4000 },
      );
      this.tab.set(0);
      await this.load();
    } catch (err: unknown) {
      const message = apiErrorMessage(err, 'No se pudieron generar las liquidaciones');
      this.error.set(message);
      this.snackBar.open(message, 'Cerrar', { duration: 6000 });
    } finally {
      this.saving.set(false);
    }
  }

  /** Genera la liquidación de un empleado y muestra el detalle con su PDF. */
  async generarUna(employee: EmployeeResponse): Promise<void> {
    this.saving.set(true);
    this.error.set('');
    try {
      const liq = await firstValueFrom(
        this.api.createLiquidacion(employee.id, this.genYear(), this.genMonth()),
      );
      await this.load();
      this.verDetalle(liq, true);
    } catch (err: unknown) {
      const message = apiErrorMessage(err, 'No se pudo generar la liquidación');
      this.error.set(message);
      this.snackBar.open(message, 'Cerrar', { duration: 6000 });
    } finally {
      this.saving.set(false);
    }
  }

  verDetalle(liq: LiquidacionResponse, fromGenerate = false): void {
    this.dialog.open<LiquidacionDetailDialog, LiquidacionDetailData>(LiquidacionDetailDialog, {
      data: { liquidacion: liq, fromGenerate },
      width: '760px',
      maxWidth: '95vw',
    });
  }

  async marcarPagada(liq: LiquidacionResponse): Promise<void> {
    const data: ConfirmData = {
      title: 'Marcar como pagada',
      message: `¿Marcar como pagada la liquidación de ${liq.employeeName} por ${
        formatMoney(liq.liquidoAPagar)
      }?`,
      confirmText: 'Marcar pagada',
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(() => firstValueFrom(this.api.marcarLiquidacionPagada(liq.id)), 'Liquidación pagada');
  }

  async openHoraExtra(employee: EmployeeResponse): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<HoraExtraDialog, HoraExtraDialogData, HoraExtraRequest | undefined>(HoraExtraDialog, {
          data: { employee },
          width: '520px',
          maxWidth: '95vw',
        })
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () => firstValueFrom(this.api.addHoraExtra(employee.id, result)),
      'Horas extra registradas',
    );
  }

  async deleteHoraExtra(h: HoraExtraResponse): Promise<void> {
    const data: ConfirmData = {
      title: 'Eliminar horas extra',
      message: `¿Eliminar el registro del ${formatDate(h.date)} por ${formatMoney(h.monto)}?`,
      confirmText: 'Eliminar',
      danger: true,
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(() => firstValueFrom(this.api.deleteHoraExtra(h.id)), 'Registro eliminado');
  }

  async openAdvance(employee: EmployeeResponse): Promise<void> {
    const yaRegistrado = this.anticiposDe(employee.id).reduce((s, a) => s + a.amount, 0);
    const result = await firstValueFrom(
      this.dialog
        .open<AdvanceDialog, AdvanceDialogData, AdvanceRequest | undefined>(
          AdvanceDialog,
          { data: { employee, yaRegistrado }, width: '520px', maxWidth: '95vw' },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () => firstValueFrom(this.api.addAdvance(employee.id, result)),
      'Anticipo registrado',
    );
  }

  async deleteAdvance(a: AdvanceResponse): Promise<void> {
    const data: ConfirmData = {
      title: 'Eliminar anticipo',
      message: `¿Eliminar el anticipo de ${formatDate(a.date)} por ${formatMoney(a.amount)}?`,
      confirmText: 'Eliminar',
      danger: true,
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(() => firstValueFrom(this.api.deleteAdvance(a.id)), 'Anticipo eliminado');
  }

  async openLicencia(employee: EmployeeResponse): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<LicenciaDialog, LicenciaDialogData, LicenciaRequest | undefined>(
          LicenciaDialog,
          { data: { employee }, width: '520px', maxWidth: '95vw' },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () => firstValueFrom(this.api.addLicencia(employee.id, result)),
      'Licencia registrada',
    );
  }

  async deleteLicencia(l: LicenciaResponse): Promise<void> {
    const data: ConfirmData = {
      title: 'Eliminar licencia',
      message: `¿Eliminar la licencia médica del ${formatDate(l.startDate)} al ${formatDate(
        l.endDate,
      )}?`,
      confirmText: 'Eliminar',
      danger: true,
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(() => firstValueFrom(this.api.deleteLicencia(l.id)), 'Licencia eliminada');
  }

  private async run(action: () => Promise<unknown>, successMessage: string): Promise<void> {
    this.saving.set(true);
    this.error.set('');
    try {
      await action();
      this.snackBar.open(successMessage, 'Cerrar', { duration: 3500 });
      await this.load();
    } catch (err: unknown) {
      const message = apiErrorMessage(err);
      this.error.set(message);
      this.snackBar.open(message, 'Cerrar', { duration: 6000 });
    } finally {
      this.saving.set(false);
    }
  }
}
