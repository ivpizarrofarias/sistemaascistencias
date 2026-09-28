import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
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
    // El validador mira el tipo elegido: el campo que no aplica se valida solo
    // si aplica. Con un validador fijo, "Guardar permiso" no cerraba el diálogo
    // sin avisar, y mat-error no se mostraba porque el control parecía válido.
    horas: [0, [this.cantidadValidator('POR_HORA')]],
    dias: [1, [this.cantidadValidator('POR_DIA')]],
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
    // Al cambiar de tipo, el campo que recién dejó de aplicar debe dejar de
    // invalidar el formulario.
    this.form.controls.horas.updateValueAndValidity();
    this.form.controls.dias.updateValueAndValidity();
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

  /**
   * Valida la cantidad del permiso solo cuando el tipo coincide. Los campos
   * numéricos llegan como string desde los inputs, y uno vacío queda en `""`:
   * sin normalizar, el guardado se bloqueaba sin mostrar error.
   */
  private cantidadValidator(tipo: PermisoTipo) {
    return (control: { value: unknown }): ValidationErrors | null => {
      if (this.form?.controls.tipo.value !== tipo) return null;
      const n = Number(control.value);
      if (tipo === 'POR_HORA') {
        return n > 0 ? null : { horasInvalidas: true };
      }
      return n >= 1 ? null : { diasInvalidos: true };
    };
  }

  get horasInvalidas(): boolean {
    return this.form.controls.horas.hasError('horasInvalidas');
  }

  get diasInvalidos(): boolean {
    return this.form.controls.dias.hasError('diasInvalidos');
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
      // El campo que no aplica al tipo elegido va en null, no en 0: el backend
      // exige mínimo 1 día, y mandar 0 rechazaba el permiso por completo.
      dias: porHora ? null : Number(v.dias),
      motivo: v.motivo.trim(),
      formaCompensacion: v.formaCompensacion,
    });
  }
}
