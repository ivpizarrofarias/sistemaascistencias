import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';

import {
  DIAS_SEMANA,
  EmployeeResponse,
  ScheduleDay,
  WeeklySchedule,
  WeeklyScheduleSave,
} from '../../core/models/api.models';
import { addDaysIso, formatDate } from '../../core/utils/format';

export interface ScheduleWeekData {
  employee: EmployeeResponse;
  /** Horario existente de esa semana, o `null` si es nueva. */
  schedule: WeeklySchedule | null;
  /** Lunes de la semana en edición (`yyyy-MM-dd`). */
  monday: string;
}

/** Editor de la semana de un empleado: días que trabaja y sus horas. */
@Component({
  selector: 'app-schedule-week-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
  ],
  templateUrl: './schedule-week-dialog.html',
  styleUrl: './schedule-week-dialog.scss',
})
export class ScheduleWeekDialog {
  private readonly ref =
    inject(MatDialogRef<ScheduleWeekDialog, WeeklyScheduleSave | undefined>);
  readonly data = inject<ScheduleWeekData>(MAT_DIALOG_DATA);

  readonly days = DIAS_SEMANA;
  readonly columns = ['dia', 'trabaja', 'entrada', 'salida'];

  readonly range = `${formatDate(this.data.monday)} al ${formatDate(
    addDaysIso(this.data.monday, 5),
  )}`;

  readonly label = signal(this.data.schedule?.label ?? '');
  readonly dayList = signal<ScheduleDay[]>(buildDays(this.data));

  cancel(): void {
    this.ref.close(undefined);
  }

  toggleWorked(index: number, worked: boolean): void {
    this.dayList.update((days) =>
      days.map((d, i) => (i === index ? { ...d, worked } : d)),
    );
  }

  setTime(index: number, field: 'startTime' | 'endTime', value: string): void {
    this.dayList.update((days) =>
      days.map((d, i) => (i === index ? { ...d, [field]: value || null } : d)),
    );
  }

  onTimeInput(index: number, field: 'startTime' | 'endTime', raw: string): void {
    // `input[type=time]` entrega "HH:mm"; algunos navegadores agregan ":00".
    const value = raw ? raw.slice(0, 5) : '';
    this.setTime(index, field, value);
  }

  submit(): void {
    const days = this.dayList();

    this.ref.close({
      employeeId: this.data.employee.id,
      startDate: this.data.monday,
      days: days.map((d) => ({
        dayNumber: d.dayNumber,
        worked: d.worked,
        // Si no trabaja, no se envían horas.
        startTime: d.worked ? d.startTime : null,
        endTime: d.worked ? d.endTime : null,
      })),
    });
  }
}

/**
 * Construye los 6 días (lunes a sábado) a editar.
 *
 * Si la semana ya existe se parte de sus datos; si no, se ofrece una plantilla
 * de lunes a viernes de 09:00 a 18:00.
 */
function buildDays(data: ScheduleWeekData): ScheduleDay[] {
  const existing = data.schedule?.days;
  if (existing && existing.length > 0) {
    return DIAS_SEMANA.map((_, i) => {
      const found = existing.find((d) => d.dayNumber === i + 1);
      return {
        id: found?.id ?? null,
        dayNumber: i + 1,
        worked: found?.worked ?? false,
        startTime: found?.startTime ?? null,
        endTime: found?.endTime ?? null,
      };
    });
  }

  return DIAS_SEMANA.map((_, i) => ({
    id: null,
    dayNumber: i + 1,
    worked: i < 5,
    startTime: '09:00',
    endTime: '18:00',
  }));
}
