import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
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

import { EmployeeResponse, LicenciaRequest } from '../../core/models/api.models';
import { daysBetweenIso, todayIso } from '../../core/utils/format';

export interface LicenciaDialogData {
  employee: EmployeeResponse;
}

/**
 * Alta de licencia médica. El empleador no paga subsidio, así que todos los días
 * de la licencia se descuentan del sueldo en la liquidación.
 */
@Component({
  selector: 'app-licencia-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
  ],
  templateUrl: './licencia-dialog.html',
  styleUrl: './licencia-dialog.scss',
})
export class LicenciaDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject<MatDialogRef<LicenciaDialog, LicenciaRequest | undefined>>(
    MatDialogRef,
  );
  readonly data = inject<LicenciaDialogData>(MAT_DIALOG_DATA);

  readonly form = this.fb.nonNullable.group({
    startDate: [todayIso(), [Validators.required]],
    endDate: [todayIso(), [Validators.required]],
    motivo: ['', [Validators.required]],
  });

  /** Días que se descontarán, inclusivo entre ambas fechas. */
  get dias(): number {
    const { startDate, endDate } = this.form.getRawValue();
    if (!startDate || !endDate) return 0;
    const diff = daysBetweenIso(startDate, endDate) + 1;
    return diff > 0 ? diff : 0;
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
    this.ref.close({
      startDate: v.startDate,
      endDate: v.endDate,
      motivo: v.motivo.trim(),
    });
  }
}
