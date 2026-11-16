import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { SystemUser, SystemUserRequest } from '../../core/models/api.models';

export interface UserDialogData {
  initial: SystemUser | null;
}

@Component({
  selector: 'app-user-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.initial ? 'Editar usuario' : 'Nuevo usuario del sistema' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form" (ngSubmit)="submit()">
        <div class="grid">
          <mat-form-field appearance="outline">
            <mat-label>Usuario</mat-label>
            <input matInput formControlName="username" required />
            <mat-error>El usuario es obligatorio</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Nombre completo</mat-label>
            <input matInput formControlName="fullName" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Rol</mat-label>
            <mat-select formControlName="role" required>
              <mat-option value="ADMIN">Administrador</mat-option>
              <mat-option value="SUPERVISOR">Supervisor</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>{{ data.initial ? 'Nueva contraseña (opcional)' : 'Contraseña' }}</mat-label>
            <input matInput [type]="showPassword ? 'text' : 'password'" formControlName="password" />
            <button mat-icon-button matSuffix type="button" (click)="showPassword = !showPassword">
              <mat-icon>{{ showPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
            @if (!data.initial && form.controls.password.touched && form.controls.password.invalid) {
              <mat-error>La contraseña es obligatoria (mín. 4 caracteres)</mat-error>
            }
            @if (data.initial) {
              <mat-hint>Déjala en blanco para no cambiarla</mat-hint>
            }
          </mat-form-field>
        </div>

        <mat-slide-toggle formControlName="enabled">Usuario habilitado</mat-slide-toggle>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="ref.close()">Cancelar</button>
      <button mat-flat-button color="primary" type="button" (click)="submit()">
        {{ data.initial ? 'Guardar' : 'Crear' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form { display: flex; flex-direction: column; gap: 12px; min-width: min(520px, 80vw); padding-top: 4px; }
    .grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
    @media (max-width: 720px) { .grid { grid-template-columns: 1fr; } }
  `],
})
export class UserDialog {
  private readonly fb = inject(FormBuilder);
  readonly ref = inject(MatDialogRef<UserDialog, SystemUserRequest | undefined>);
  readonly data = inject<UserDialogData>(MAT_DIALOG_DATA);

  showPassword = false;

  readonly form = this.fb.nonNullable.group({
    username: [this.data.initial?.username ?? '', [Validators.required, Validators.maxLength(60)]],
    fullName: [this.data.initial?.fullName ?? ''],
    role: [this.data.initial?.role ?? 'SUPERVISOR', [Validators.required]],
    password: ['', this.data.initial ? [Validators.minLength(4)] : [Validators.required, Validators.minLength(4)]],
    enabled: [this.data.initial?.enabled ?? true],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.ref.close({
      username: v.username.trim(),
      fullName: v.fullName?.trim() || null,
      role: v.role as 'ADMIN' | 'SUPERVISOR',
      password: v.password?.trim() || null,
      enabled: v.enabled,
    });
  }
}
