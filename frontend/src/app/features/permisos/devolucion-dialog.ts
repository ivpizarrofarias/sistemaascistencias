import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import { DevolucionRequest, PERMISO_TIPO_LABEL, PermisoLaboralResponse } from '../../core/models/api.models';
import { formatDate } from '../../core/utils/format';

export interface DevolucionData {
  permiso: PermisoLaboralResponse;
}

/** Registra el tiempo (horas o días) que el trabajador ya devolvió. */
@Component({
  selector: 'app-devolucion-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
  ],
  templateUrl: './devolucion-dialog.html',
  styleUrl: './devolucion-dialog.scss',
})
export class DevolucionDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject<MatDialogRef<DevolucionDialog, DevolucionRequest | undefined>>(
    MatDialogRef,
  );
  readonly data = inject<DevolucionData>(MAT_DIALOG_DATA);

  readonly permiso = this.data.permiso;
  readonly formatDate = formatDate;
  readonly tipoLabel = PERMISO_TIPO_LABEL;
  readonly porHora = this.permiso.tipo === 'POR_HORA';

  /** Saldo que aún falta devolver; es el máximo permitido en el input. */
  readonly saldoPendiente = computed(() =>
    this.porHora
      ? Math.max((this.permiso.horas ?? 0) - (this.permiso.horasDevueltas ?? 0), 0)
      : Math.max(this.permiso.dias - this.permiso.diasDevueltos, 0),
  );

  readonly form = this.fb.nonNullable.group({
    cantidad: [0, [Validators.required, Validators.min(0.01)]],
  });

  onCantidadChange(value: string): void {
    this.form.controls.cantidad.setValue(Number(value) || 0);
  }

  cancel(): void {
    this.ref.close(undefined);
  }

  submit(): void {
    if (this.form.invalid || this.saldoPendiente() <= 0) {
      this.form.markAllAsTouched();
      return;
    }

    const cantidad = Number(this.form.controls.cantidad.value);
    this.ref.close(this.porHora ? { horasDevueltas: cantidad } : { diasDevueltos: cantidad });
  }
}
