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

import { AdvanceRequest, EmployeeResponse } from '../../core/models/api.models';
import { formatMoney, todayIso } from '../../core/utils/format';

export interface AdvanceDialogData {
  employee: EmployeeResponse;
  /** Suma ya registrada en el período; se usa para mostrar el tope del 15%. */
  yaRegistrado: number;
}

/** Alta de anticipo de sueldo. */
@Component({
  selector: 'app-advance-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
  ],
  templateUrl: './advance-dialog.html',
  styleUrl: './advance-dialog.scss',
})
export class AdvanceDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject<MatDialogRef<AdvanceDialog, AdvanceRequest | undefined>>(
    MatDialogRef,
  );
  readonly data = inject<AdvanceDialogData>(MAT_DIALOG_DATA);

  readonly formatMoney = formatMoney;

  /** El backend limita el total de anticipos al 15% del sueldo base. */
  readonly tope = Math.round(this.data.employee.baseSalary * 0.15);
  readonly disponible = Math.max(this.tope - this.data.yaRegistrado, 0);

  readonly form = this.fb.nonNullable.group({
    date: [todayIso(), [Validators.required]],
    amount: [0, [Validators.required, Validators.min(1)]],
    description: [''],
  });

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
      date: v.date,
      amount: Number(v.amount),
      description: v.description.trim() || null,
    });
  }
}
