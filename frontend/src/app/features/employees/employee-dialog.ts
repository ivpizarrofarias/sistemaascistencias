import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
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

import { EmployeeRequest, EmployeeResponse } from '../../core/models/api.models';
import { formatearMiles, formatearRut, parsearMiles, validarRut } from '../../core/utils/rut';

export interface EmployeeFormData {
  /** `null` para crear, el empleado para editar. */
  initial: EmployeeResponse | null;
}

@Component({
  selector: 'app-employee-dialog',
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
  templateUrl: './employee-dialog.html',
  styleUrl: './employee-dialog.scss',
})
export class EmployeeDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject(MatDialogRef<EmployeeDialog, EmployeeRequest | undefined>);
  readonly data = inject<EmployeeFormData>(MAT_DIALOG_DATA);

  readonly isEdit = Boolean(this.data.initial);

  /** Se muestra el error del RUT recién cuando el usuario lo ha tocado. */
  readonly rutTouched = signal(false);

  readonly form = this.fb.nonNullable.group({
    rut: ['', [Validators.required]],
    hireDate: ['', [Validators.required]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    phone: [''],
    email: ['', [Validators.email]],
    baseSalary: ['0', [Validators.required]],
    dependents: [0, [Validators.min(0)]],
    pensionSystem: ['AFP'],
    afpFund: [''],
    healthSystem: ['FONASA'],
  });

  constructor() {
    const e = this.data.initial;
    if (e) {
      this.form.patchValue({
        rut: formatearRut(e.rut),
        hireDate: e.hireDate,
        firstName: e.firstName,
        lastName: e.lastName,
        phone: e.phone ?? '',
        email: e.email ?? '',
        baseSalary: formatearMiles(e.baseSalary),
        dependents: e.dependents,
        pensionSystem: e.pensionSystem ?? 'AFP',
        afpFund: e.afpFund ?? '',
        healthSystem: e.healthSystem ?? 'FONASA',
      });
    }
  }

  /** El RUT es obligatorio y debe pasar la validación de módulo 11. */
  get rutValue(): string {
    return this.form.controls.rut.value;
  }

  get rutValido(): boolean {
    return this.rutValue.trim().length > 0 && validarRut(this.rutValue);
  }

  get mostrarErrorRut(): boolean {
    return this.rutTouched() && this.rutValue.trim().length > 0 && !this.rutValido;
  }

  onRutInput(value: string): void {
    this.form.controls.rut.setValue(formatearRut(value));
  }

  onSalaryInput(value: string): void {
    this.form.controls.baseSalary.setValue(formatearMiles(value));
  }

  cancel(): void {
    this.ref.close(undefined);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.rutTouched.set(true);
      return;
    }

    if (!this.rutValido) {
      this.rutTouched.set(true);
      return;
    }

    const v = this.form.getRawValue();
    this.ref.close({
      rut: v.rut,
      firstName: v.firstName.trim(),
      lastName: v.lastName.trim(),
      phone: v.phone.trim() || null,
      email: v.email.trim() || null,
      hireDate: v.hireDate,
      baseSalary: parsearMiles(v.baseSalary),
      dependents: Number(v.dependents) || 0,
      pensionSystem: v.pensionSystem,
      afpFund: v.afpFund.trim() || null,
      healthSystem: v.healthSystem,
    });
  }
}
