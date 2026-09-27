import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';
import { map } from 'rxjs/operators';

import {
  AttendanceResponse,
  AttendanceUpdateRequest,
  EmployeeResponse,
} from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import {
  firstDayOfMonthIso,
  formatDate,
  formatTime,
  normalizeText,
  todayIso,
} from '../../core/utils/format';
import { PageHeader } from '../../shared/page-header';
import { EmptyState } from '../../shared/states';
import { ConfirmDialog, ConfirmData } from '../../shared/confirm-dialog';
import { AttendanceEditDialog, AttendanceEditData } from './attendance-edit-dialog';

@Component({
  selector: 'app-attendance',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    PageHeader,
    EmptyState,
  ],
  templateUrl: './attendance.html',
  styleUrl: './attendance.scss',
})
export class AttendancePage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  /** Todos los trabajadores de la empresa: la tabla de selección. */
  readonly employees = signal<EmployeeResponse[]>([]);
  /** Trabajador elegido; su asistencia se muestra en el detalle. */
  readonly selected = signal<EmployeeResponse | null>(null);
  /** Asistencias del trabajador elegido en el rango visible. */
  readonly rows = signal<AttendanceResponse[]>([]);
  readonly search = signal('');
  readonly from = signal(firstDayOfMonthIso(new Date().getFullYear(), new Date().getMonth() + 1));
  readonly to = signal(todayIso());
  readonly loading = signal(true);
  readonly loadingDetail = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');

  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  readonly employeeColumns = ['trabajador', 'rut', 'sueldo', 'estado', 'accion'];

  readonly attendanceColumns = [
    'fecha',
    'entrada',
    'colacionSalida',
    'colacionEntrada',
    'salida',
    'estado',
    'acciones',
  ];

  /** Tabla de trabajadores filtrada por nombre, RUT o código de marcaje. */
  readonly employeesFiltrados = computed(() => {
    const q = normalizeText(this.search());
    if (!q) return this.employees();
    return this.employees().filter((e) =>
      normalizeText([e.fullName, e.rut, e.code].join(' ')).includes(q),
    );
  });

  /** Días del trabajador seleccionado dentro del rango. */
  readonly daysInRange = computed(() => {
    const a = this.from();
    const b = this.to();
    if (!a || !b) return 0;
    const inicio = Date.parse(`${a}T00:00:00`);
    const fin = Date.parse(`${b}T00:00:00`);
    if (Number.isNaN(inicio) || Number.isNaN(fin) || fin < inicio) return 0;
    return Math.round((fin - inicio) / 86_400_000) + 1;
  });

  readonly resumen = computed(() => {
    const rows = this.rows();
    return {
      total: rows.length,
      completos: rows.filter((a) => a.complete && !a.absence && !a.medicalLicense).length,
      incompletos: rows.filter((a) => !a.complete && !a.absence && !a.medicalLicense).length,
      inasistencias: rows.filter((a) => a.absence).length,
      licencias: rows.filter((a) => a.medicalLicense).length,
    };
  });

  ngOnInit(): void {
    void this.load();
  }

  /** Carga la plantilla de trabajadores; no consulta asistencias todavía. */
  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      this.employees.set(await firstValueFrom(this.api.listEmployees()));
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudo cargar la lista de trabajadores'));
    } finally {
      this.loading.set(false);
    }
  }

  onSearchChange(value: string): void {
    this.search.set(value);
  }

  onDateChange(field: 'from' | 'to', value: string): void {
    if (field === 'from') this.from.set(value);
    else this.to.set(value);
  }

  usarMesActual(): void {
    const now = new Date();
    this.from.set(firstDayOfMonthIso(now.getFullYear(), now.getMonth() + 1));
    this.to.set(todayIso());
  }

  /** Elige un trabajador y carga su asistencia del período. */
  async seleccionar(e: EmployeeResponse): Promise<void> {
    this.selected.set(e);
    await this.loadSelected();
  }

  /** Vuelve a consultar la asistencia del trabajador elegido. */
  async loadSelected(): Promise<void> {
    const e = this.selected();
    if (!e) return;
    this.loadingDetail.set(true);
    this.error.set('');
    try {
      this.rows.set(
        await firstValueFrom(this.api.listAttendance(e.id, this.from() || undefined, this.to() || undefined)),
      );
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudo cargar la asistencia del trabajador'));
      this.rows.set([]);
    } finally {
      this.loadingDetail.set(false);
    }
  }

  /** Alterna el estado completo/incompleto del día. */
  async toggleComplete(a: AttendanceResponse): Promise<void> {
    await this.run(
      () =>
        firstValueFrom(
          this.api.updateAttendance(a.id, { complete: !a.complete } as AttendanceUpdateRequest),
        ),
      a.complete ? 'Marcado como incompleto' : 'Marcado como completo',
    );
  }

  /** Marca o revierte una inasistencia no justificada. */
  async toggleAbsence(a: AttendanceResponse): Promise<void> {
    const absence = !a.absence;
    const data: ConfirmData = {
      title: absence ? 'Marcar inasistencia' : 'Revertir inasistencia',
      message: `¿Marcar a ${a.employeeName} como ${
        absence ? 'ausente' : 'presente'
      } el ${formatDate(a.date)}?`,
      confirmText: absence ? 'Marcar ausente' : 'Revertir',
      danger: absence,
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(
      () => firstValueFrom(this.api.setAbsence(a.employeeId, a.date, absence)),
      absence ? 'Inasistencia registrada' : 'Inasistencia revertida',
    );
  }

  async openEdit(a: AttendanceResponse): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<AttendanceEditDialog, AttendanceEditData, AttendanceUpdateRequest | undefined>(
          AttendanceEditDialog,
          { data: { attendance: a }, width: '520px', maxWidth: '95vw' },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () => firstValueFrom(this.api.updateAttendance(a.id, result)),
      'Asistencia actualizada',
    );
  }

  private async run(action: () => Promise<unknown>, successMessage: string): Promise<void> {
    this.saving.set(true);
    this.error.set('');
    try {
      await action();
      this.snackBar.open(successMessage, 'Cerrar', { duration: 3500 });
      await this.loadSelected();
    } catch (err: unknown) {
      const message = apiErrorMessage(err);
      this.error.set(message);
      this.snackBar.open(message, 'Cerrar', { duration: 6000 });
    } finally {
      this.saving.set(false);
    }
  }
}
