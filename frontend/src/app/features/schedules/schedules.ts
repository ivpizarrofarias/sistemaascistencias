import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import {
  DIAS_SEMANA,
  EmployeeResponse,
  WeeklySchedule,
  WeeklyScheduleSave,
} from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import { NotifyService } from '../../core/services/notify.service';
import { FileDownloadService } from '../../core/services/file-download.service';
import { addDaysIso, formatDate, formatTime, mondayOfIso, todayIso } from '../../core/utils/format';
import { PageHeader } from '../../shared/page-header';
import { EmptyState, Loading } from '../../shared/states';
import { ConfirmDialog, ConfirmData } from '../../shared/confirm-dialog';
import { ScheduleWeekDialog, ScheduleWeekData } from './schedule-week-dialog';

/** Fila de la tabla: empleado + su horario de la semana seleccionada. */
interface ScheduleRow {
  employee: EmployeeResponse;
  schedule: WeeklySchedule | null;
}

@Component({
  selector: 'app-schedules',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule,
    PageHeader,
    EmptyState,
    Loading,
  ],
  templateUrl: './schedules.html',
  styleUrl: './schedules.scss',
})
export class SchedulesPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotifyService);
  private readonly files = inject(FileDownloadService);

  readonly days = DIAS_SEMANA;
  readonly formatDate = formatDate;

  /** Solo los empleados habilitados pueden tener horario. */
  readonly employees = signal<EmployeeResponse[]>([]);
  readonly rows = signal<ScheduleRow[]>([]);
  readonly weekStart = signal(mondayOfIso(todayIso()));
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');

  readonly columns = ['empleado', ...DIAS_SEMANA.map((_, i) => `d${i + 1}`), 'acciones'];

  /** Rango de la semana visible, para el encabezado. */
  readonly weekRange = computed(() => {
    const monday = this.monday();
    return `${formatDate(monday)} al ${formatDate(addDaysIso(monday, 5))}`;
  });

  /** El backend indexa las semanas por su lunes. */
  readonly monday = computed(() => mondayOfIso(this.weekStart()));

  ngOnInit(): void {
    void this.loadEmployees();
  }

  private async loadEmployees(): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      const all = await firstValueFrom(this.api.listEmployees());
      const enabled = all.filter((e) => e.enabled);
      this.employees.set(enabled);
      await this.loadWeek(enabled);
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudieron cargar los empleados'));
    } finally {
      this.loading.set(false);
    }
  }

  /** Pide en paralelo el horario de la semana de cada empleado. */
  private async loadWeek(employees = this.employees()): Promise<void> {
    if (employees.length === 0) {
      this.rows.set([]);
      return;
    }

    const monday = this.monday();
    const results = await firstValueFrom(
      forkJoin(
        employees.map((employee) =>
          this.api
            .getScheduleWeek(employee.id, monday)
            // Un empleado sin horario no debe cortar la carga de los demás.
            .pipe(catchError(() => of(null))),
        ),
      ),
    );

    this.rows.set(
      employees.map((employee, i) => ({ employee, schedule: results[i] })),
    );
  }

  onWeekChange(value: string): void {
    if (!value) return;
    this.weekStart.set(value);
    void this.loadWeek();
  }

  goToCurrentWeek(): void {
    this.weekStart.set(mondayOfIso(todayIso()));
    void this.loadWeek();
  }

  shiftWeek(weeks: number): void {
    this.weekStart.set(addDaysIso(this.monday(), weeks * 7));
    void this.loadWeek();
  }

  /** Descripción de un día: "09:00-18:00" si trabaja, "—" si no. */
  dayText(row: ScheduleRow, dayNumber: number): string {
    const day = row.schedule?.days?.find((d) => d.dayNumber === dayNumber);
    if (!day || !day.worked) return '—';
    if (day.startTime && day.endTime) {
      return `${formatTime(day.startTime)}-${formatTime(day.endTime)}`;
    }
    return 'Sí';
  }

  hasSchedule(row: ScheduleRow): boolean {
    return Boolean(row.schedule?.id);
  }

  async openEditor(row: ScheduleRow): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<ScheduleWeekDialog, ScheduleWeekData, WeeklyScheduleSave | undefined>(
          ScheduleWeekDialog,
          {
            data: { employee: row.employee, schedule: row.schedule, monday: this.monday() },
            width: '640px',
            maxWidth: '95vw',
          },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () =>
        row.schedule?.id
          ? firstValueFrom(this.api.updateSchedule(row.schedule.id, result))
          : firstValueFrom(this.api.saveSchedule(result)),
      'Horario guardado correctamente',
    );
  }

  async downloadPdf(row: ScheduleRow): Promise<void> {
    this.error.set('');
    try {
      const blob = await firstValueFrom(
        this.api.downloadSchedulePdf(row.employee.id, this.monday()),
      );
      this.files.save(blob, `horario-${row.employee.code}-${this.monday()}.pdf`);
    } catch (err: unknown) {
      const message = apiErrorMessage(err, 'No se pudo generar el PDF del horario');
      this.error.set(message);
      this.notify.error(message);
    }
  }

  async remove(row: ScheduleRow): Promise<void> {
    const id = row.schedule?.id;
    if (!id) return;

    const data: ConfirmData = {
      title: 'Eliminar horario',
      message: `¿Eliminar el horario de ${row.employee.fullName} para la semana del ${formatDate(
        this.monday(),
      )}?`,
      confirmText: 'Eliminar',
      danger: true,
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(
      () => firstValueFrom(this.api.deleteSchedule(id)),
      'Horario eliminado',
    );
  }

  private async run(action: () => Promise<unknown>, successMessage: string): Promise<void> {
    this.saving.set(true);
    this.error.set('');
    try {
      await action();
      this.notify.success(successMessage);
      await this.loadWeek();
    } catch (err: unknown) {
      const message = apiErrorMessage(err);
      this.error.set(message);
      this.notify.error(message);
    } finally {
      this.saving.set(false);
    }
  }
}
