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
import { MatSelectModule } from '@angular/material/select';

import {
  EmployeeResponse,
  FormaCompensacion,
  FORMA_COMPENSACION_LABEL,
  PERMISO_TIPO_LABEL,
  PermisoLaboralRequest,
  PermisoTipo,
} from '../../core/models/api.models';
import { addDaysIso, formatDate } from '../../core/utils/format';

export interface PermisoFormResult extends PermisoLaboralRequest {
  employeeId: number;
}

export interface PermisoDialogData {
  employees: EmployeeResponse[];
}

/** Alta de un permiso a nombre de un empleado (queda APROBADO). */
@Component({
  selector: 'app-permiso-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  templateUrl: './permiso-dialog.html',
  styleUrl: './permiso-dialog.scss',
})
export class PermisoDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject<MatDialogRef<PermisoDialog, PermisoFormResult | undefined>>(
    MatDialogRef,
  );
  readonly data = inject<PermisoDialogData>(MAT_DIALOG_DATA);

  readonly tipoLabel = PERMISO_TIPO_LABEL;
  readonly formatDate = formatDate;
  readonly compensacionEntries = Object.entries(FORMA_COMPENSACION_LABEL) as [
    FormaCompensacion,
    string,
  ][];

  readonly form = this.fb.nonNullable.group({
    employeeId: [0, [Validators.required, Validators.min(1)]],
    date: ['', [Validators.required]],
    tipo: ['POR_HORA' as PermisoTipo, [Validators.required]],
    horas: [0, [Validators.min(0.01)]],
    dias: [1, [Validators.min(1)]],
    motivo: ['', [Validators.required]],
    formaCompensacion: ['DESCUENTO' as FormaCompensacion, [Validators.required]],
  });

  readonly tipo = signal<PermisoTipo>('POR_HORA');
  readonly compensacion = signal<FormaCompensacion>('DESCUENTO');
  readonly date = signal('');
  readonly dias = signal(1);

  /** Fechas concretas que abarca el permiso, para previsualizarlas. */
  readonly fechas = computed(() => {
    if (this.tipo() !== 'POR_DIA') return [];
    const inicio = this.date();
    const cantidad = this.dias();
    if (!inicio || cantidad < 1) return [];
    return Array.from({ length: cantidad }, (_, i) => addDaysIso(inicio, i));
  });

  onTipoChange(value: PermisoTipo): void {
    this.tipo.set(value);
    this.form.controls.tipo.setValue(value);
  }

  onCompensacionChange(value: FormaCompensacion): void {
    this.compensacion.set(value);
    this.form.controls.formaCompensacion.setValue(value);
  }

  onDateChange(value: string): void {
    this.date.set(value);
    this.form.controls.date.setValue(value);
  }

  onDiasChange(value: string): void {
    const dias = Number(value) || 0;
    this.dias.set(dias);
    this.form.controls.dias.setValue(dias);
  }

  cancel(): void {
    this.ref.close(undefined);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const porHora = v.tipo === 'POR_HORA';

    this.ref.close({
      employeeId: Number(v.employeeId),
      date: v.date,
      tipo: v.tipo,
      horas: porHora ? Number(v.horas) : null,
      dias: porHora ? 0 : Number(v.dias),
      motivo: v.motivo.trim(),
      formaCompensacion: v.formaCompensacion,
    });
  }
}
