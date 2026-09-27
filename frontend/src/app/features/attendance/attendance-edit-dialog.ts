import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import {
  AttendanceResponse,
  AttendanceUpdateRequest,
} from '../../core/models/api.models';
import { formatDate } from '../../core/utils/format';

export interface AttendanceEditData {
  attendance: AttendanceResponse;
}

/** Corrige los tiempos de marcaje de un día ya registrado. */
@Component({
  selector: 'app-attendance-edit-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './attendance-edit-dialog.html',
  styleUrl: './attendance-edit-dialog.scss',
})
export class AttendanceEditDialog {
  private readonly ref = inject<
    MatDialogRef<AttendanceEditDialog, AttendanceUpdateRequest | undefined>
  >(MatDialogRef);
  readonly data = inject<AttendanceEditData>(MAT_DIALOG_DATA);

  readonly attendance = this.data.attendance;
  readonly dateLabel = formatDate(this.attendance.date);

  // Los tiempos se manejan como "HH:mm"; el backend los guarda como LocalTime.
  readonly entryTime = signal(clip(this.attendance.entryTime));
  readonly colacionSalida1 = signal(clip(this.attendance.colacionSalida1));
  readonly colacionEntrada1 = signal(clip(this.attendance.colacionEntrada1));
  readonly exitTime = signal(clip(this.attendance.exitTime));

  set(field: 'entryTime' | 'colacionSalida1' | 'colacionEntrada1' | 'exitTime', value: string) {
    const v = value.slice(0, 5);
    switch (field) {
      case 'entryTime':
        this.entryTime.set(v);
        break;
      case 'colacionSalida1':
        this.colacionSalida1.set(v);
        break;
      case 'colacionEntrada1':
        this.colacionEntrada1.set(v);
        break;
      case 'exitTime':
        this.exitTime.set(v);
        break;
    }
  }

  cancel(): void {
    this.ref.close(undefined);
  }

  submit(): void {
    this.ref.close({
      entryTime: this.entryTime() || null,
      colacionSalida1: this.colacionSalida1() || null,
      colacionEntrada1: this.colacionEntrada1() || null,
      exitTime: this.exitTime() || null,
    });
  }
}

/** Recorta una hora a `HH:mm`, o devuelve string vacío si es null. */
function clip(value: string | null): string {
  return value ? value.slice(0, 5) : '';
}
