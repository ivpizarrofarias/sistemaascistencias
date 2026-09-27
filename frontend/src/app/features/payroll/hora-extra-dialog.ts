import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
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

import { EmployeeResponse, HoraExtraRequest } from '../../core/models/api.models';
import { formatMoney, todayIso } from '../../core/utils/format';

export interface HoraExtraDialogData {
  employee: EmployeeResponse;
}

/** Alta de horas extra: `horas` es informativo y `monto` es el pago exacto. */
@Component({
  selector: 'app-hora-extra-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
  ],
  templateUrl: './hora-extra-dialog.html',
  styleUrl: './hora-extra-dialog.scss',
})
export class HoraExtraDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject<MatDialogRef<HoraExtraDialog, HoraExtraRequest | undefined>>(
    MatDialogRef,
  );
  readonly data = inject<HoraExtraDialogData>(MAT_DIALOG_DATA);

  readonly formatMoney = formatMoney;
  readonly fecha = signal(todayIso());

  readonly form = this.fb.nonNullable.group({
    date: [todayIso(), [Validators.required]],
    horas: [1, [Validators.required, Validators.min(0.5)]],
    monto: [0, [Validators.required, Validators.min(0)]],
  });

  onDateChange(value: string): void {
    this.fecha.set(value);
    this.form.controls.date.setValue(value);
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
    this.ref.close({ date: v.date, horas: Number(v.horas), monto: Number(v.monto) });
  }
}
