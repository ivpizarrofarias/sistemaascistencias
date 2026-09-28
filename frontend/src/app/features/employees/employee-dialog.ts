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
import {
  digitoVerificadorEsperado,
  formatearMiles,
  formatearRut,
  parsearMiles,
  rutCanónico,
  validarRut,
} from '../../core/utils/rut';

/**
 * Fondos de pensiones de AFPChile. Se ofrecen en lista para evitar nombres
 * mal escritos, que después se cuelan en los recibos de liquidación.
 */
export const FONDOS_AFP = [
  'AFP CUPRUM',
  'AFP HABITAT',
  'AFP MODELO',
  'AFP PLANVITAL',
  'AFP PROVIDA',
  'AFP UNO',
] as const;

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

  readonly fondosAfp = FONDOS_AFP;

  /** Con IPS/INP el fondo de AFP no aplica, así que el campo se muestra vacío. */
  get pensionSystem(): string {
    return this.form.controls.pensionSystem.value;
  }

  /** Se muestra el error del RUT recién cuando el usuario lo ha tocado. */
  readonly rutTouched = signal(false);

  /** Evita que un doble clic cree dos empleados. */
  readonly guardado = signal(false);

  /** Marca que ya se intentó guardar, para avisar de los campos faltantes. */
  readonly intentoFallido = signal(false);

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
        afpFund: e.pensionSystem === 'IPS' ? '' : (e.afpFund ?? ''),
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

  get rutVacio(): boolean {
    return this.rutValue.trim().length === 0;
  }

  get rutTocado(): boolean {
    return this.form.controls.rut.touched;
  }

  /**
   * Dígito verificador que le corresponde al RUT escrito, para que el usuario
   * vea de inmediato si se equivocó al tipearlo en vez de un error seco.
   */
  get dvEsperado(): string | null {
    const valor = this.rutValue;
    const digitos = valor.replace(/\D/g, '');

    // Con K no hay ambigüedad: el DV ya está escrito y son todos dígitos.
    if (/[kK]/.test(valor)) return digitoVerificadorEsperado(digitos);

    // 9+ dígitos: cuerpo de 8, el último es el DV.
    if (digitos.length > 8) return digitoVerificadorEsperado(digitos.slice(0, 8));

    // 8 dígitos: solo es inequívoco si el formateador ya puso el guion
    // (cuerpo de 7 + DV). Si todavía no hay guion, no se puede sugerir nada.
    if (digitos.length === 8 && valor.includes('-')) {
      return digitoVerificadorEsperado(digitos.slice(0, 7));
    }

    return null;
  }

  /**
   * RUT tal como se va a guardar. Si el usuario escribió otra cosa, el campo lo
   * muestra y la diferencia queda a la vista en vez de ser un error seco.
   */
  get rutComoSeGuardara(): string | null {
    return rutCanónico(this.rutValue);
  }

  /**
   * Campos que impiden guardar. Sin esto, un clic en "Crear" con el formulario
   * incompleto no hacía nada visible y parecía que el botón estaba roto.
   */
  get camposFaltantes(): string[] {
    const c = this.form.controls;
    const faltantes: string[] = [];

    if (this.rutVacio) {
      faltantes.push('el RUT');
    } else if (!this.rutValido) {
      faltantes.push('un RUT válido (revisa el dígito verificador)');
    }
    if (!c.hireDate.value) faltantes.push('la fecha de ingreso');
    if (!c.firstName.value.trim()) faltantes.push('el nombre');
    if (!c.lastName.value.trim()) faltantes.push('el apellido');
    if (parsearMiles(c.baseSalary.value) <= 0) faltantes.push('el sueldo base');

    return faltantes;
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
    if (this.guardado()) return;

    this.form.markAllAsTouched();
    this.rutTouched.set(true);

    // Se avisa siempre: si el formulario está incompleto, el usuario debe ver qué falta
    // en vez de que el botón parezca no hacer nada.
    if (this.form.invalid || !this.rutValido || this.camposFaltantes.length > 0) {
      this.intentoFallido.set(true);
      return;
    }

    this.guardado.set(true);
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
      // El fondo solo aplica en AFP: mandarlo en IPS ensuciaría el recibo.
      afpFund: v.pensionSystem === 'AFP' ? v.afpFund || null : null,
      healthSystem: v.healthSystem,
    });
  }
}
