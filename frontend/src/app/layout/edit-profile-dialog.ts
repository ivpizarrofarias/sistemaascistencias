import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

import { AuthService } from '../core/services/auth.service';
import { ApiService } from '../core/services/api.service';
import { CambiarPasswordRequest } from '../core/models/api.models';

/** Lee un archivo como data URL (base64). */
function readFileAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = (err) => reject(err);
    reader.readAsDataURL(file);
  });
}

@Component({
  selector: 'app-edit-profile-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>Editar perfil</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <!-- Foto de perfil -->
        <div class="foto-seccion">
          @if (fotoActual()) {
            <div class="foto-preview">
              <img [src]="fotoActual()" alt="Foto de perfil" />
            </div>
          }
          <div class="foto-acciones">
            <input
              type="file"
              accept="image/*"
              (change)="onFotoSeleccionada($event)"
              #fileInput
              style="display: none;"
            />
            <button
              mat-button
              type="button"
              color="primary"
              (click)="fileInput.click()"
              [disabled]="saving()"
            >
              <mat-icon>image</mat-icon>
              <span>Seleccionar foto</span>
            </button>
            <button
              mat-button
              type="button"
              color="warn"
              (click)="removerFoto()"
              [disabled]="!fotoActual() || saving()"
            >
              <mat-icon>delete</mat-icon>
              <span>Remover foto</span>
            </button>
          </div>
        </div>

        <mat-divider></mat-divider>

        <mat-form-field appearance="outline">
          <mat-label>Usuario</mat-label>
          <input matInput formControlName="username" />
          <mat-icon matSuffix>person</mat-icon>
          <mat-hint>Puedes modificar tu nombre de usuario.</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Nombre completo</mat-label>
          <input matInput formControlName="fullName" />
          <mat-icon matSuffix>edit</mat-icon>
        </mat-form-field>

        <mat-divider></mat-divider>

        <mat-form-field appearance="outline">
          <mat-label>Contraseña actual</mat-label>
          <input
            matInput
            [type]="hidePasswordActual() ? 'password' : 'text'"
            formControlName="passwordActual"
          />
          <mat-icon matSuffix (click)="togglePasswordActual()">{{
            hidePasswordActual() ? 'visibility_off' : 'visibility'
          }}</mat-icon>
          <mat-hint>Dejar en blanco si no desea cambiar la contraseña.</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Nueva contraseña</mat-label>
          <input
            matInput
            [type]="hideNuevaPassword() ? 'password' : 'text'"
            formControlName="nuevaPassword"
          />
          <mat-icon matSuffix (click)="toggleNuevaPassword()">{{
            hideNuevaPassword() ? 'visibility_off' : 'visibility'
          }}</mat-icon>
          <mat-hint>Mínimo 6 caracteres. Dejar en blanco para mantener la actual.</mat-hint>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close type="button" (click)="onCancel()">
        Cancelar
      </button>
      <button
        mat-raised-button
        color="primary"
        type="button"
        [disabled]="form.invalid || saving()"
        (click)="onSave()"
      >
        {{ saving() ? 'Guardando...' : 'Guardar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    mat-form-field {
      width: 100%;
      margin-bottom: 12px;
    }
    mat-divider {
      margin: 16px 0;
    }
    mat-dialog-actions {
      gap: 8px;
    }
    .foto-seccion {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
    }
    .foto-preview {
      width: 96px;
      height: 96px;
      border-radius: 50%;
      overflow: hidden;
      border: 2px solid var(--mat-sys-outline-variant);
      background: var(--mat-sys-surface-container);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .foto-preview img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .foto-acciones {
      display: flex;
      gap: 8px;
    }
  `,
})
export class EditProfileDialog {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly dialogRef = inject(MatDialogRef<EditProfileDialog>);

  readonly saving = signal(false);
  readonly fotoActual = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    fullName: ['', Validators.required],
    passwordActual: [''],
    nuevaPassword: [''],
  });

  private hideActual = signal(true);
  private hideNueva = signal(true);

  readonly hidePasswordActual = () => this.hideActual();
  readonly hideNuevaPassword = () => this.hideNueva();

  constructor() {
    const u = this.auth.user();
    this.form.patchValue({
      username: u?.username ?? '',
      fullName: u?.fullName ?? '',
    });
    this.fotoActual.set(u?.fotoBase64 ?? null);
  }

  togglePasswordActual(): void {
    this.hideActual.update((v) => !v);
  }

  toggleNuevaPassword(): void {
    this.hideNueva.update((v) => !v);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  async onFotoSeleccionada(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    try {
      const dataUrl = await readFileAsDataUrl(file);
      this.fotoActual.set(dataUrl);
    } catch (err) {
      console.error('No se pudo leer la imagen', err);
    }
  }

  removerFoto(): void {
    this.fotoActual.set(null);
  }

  onSave(): Promise<void> {
    if (this.form.invalid) return Promise.resolve();

    const raw = this.form.getRawValue();
    const username = raw.username.trim();
    const fullName = raw.fullName;
    const passwordActual = raw.passwordActual;
    const nuevaPassword = raw.nuevaPassword;

    // Si se quiere cambiar la contraseña, validar que la actual esté presente.
    if (nuevaPassword && !passwordActual) {
      this.form.get('passwordActual')?.setErrors({ required: true });
      return Promise.resolve();
    }

    return this.guardar(username, fullName, passwordActual, nuevaPassword);
  }

  private async guardar(
    username: string,
    fullName: string,
    passwordActual: string,
    nuevaPassword: string,
  ): Promise<void> {
    try {
      this.saving.set(true);

      // 1) Guardar perfil (usuario, nombre y foto) en la base de datos.
      await this.auth.actualizarPerfil(username, fullName, this.fotoActual());

      // 2) Si se proporcionó una nueva contraseña, llamar al backend.
      if (nuevaPassword) {
        const payload: CambiarPasswordRequest = {
          passwordActual,
          nuevaPassword,
        };
        await firstValueFrom(this.api.cambiarPassword(payload));
      }

      this.dialogRef.close({
        username,
        fullName,
        fotoBase64: this.fotoActual(),
        passwordCambiada: Boolean(nuevaPassword),
      });
    } catch (err: unknown) {
      throw err;
    } finally {
      this.saving.set(false);
    }
  }
}
