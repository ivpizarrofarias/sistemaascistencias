import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { AuthService, apiErrorMessage } from '../../core/services/auth.service';

/** Imagen por defecto cuando el usuario no tiene foto de perfil. */
const DEFAULT_USER_IMAGE =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128"><rect width="128" height="128" fill="%23e0e0e0"/><circle cx="64" cy="48" r="24" fill="%239e9e9e"/><path d="M16 112c0-22 21.5-40 48-40s48 18 48 40" fill="%239e9e9e"/></svg>';

@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly hidePassword = signal(true);
  readonly loading = signal(false);
  readonly error = signal('');

  /** Imagen del usuario en login: por defecto hasta que se ingrese un usuario con foto. */
  readonly userImage = signal<string>(DEFAULT_USER_IMAGE);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  constructor() {
    // A medida que se ingresa el usuario, buscar su foto en el sistema.
    this.form.controls.username.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((username) => {
        void this.actualizarImagenUsuario(username);
      });
  }

  private async actualizarImagenUsuario(username: string): Promise<void> {
    const trimmed = (username ?? '').trim();
    if (!trimmed) {
      this.userImage.set(DEFAULT_USER_IMAGE);
      return;
    }
    try {
      const foto = await this.auth.obtenerAvatar(trimmed);
      this.userImage.set(foto || DEFAULT_USER_IMAGE);
    } catch {
      // Si falla la consulta, mostrar imagen por defecto.
      this.userImage.set(DEFAULT_USER_IMAGE);
    }
  }

  async submit(): Promise<void> {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.error.set('');
    this.loading.set(true);

    try {
      const { username, password } = this.form.getRawValue();
      // Se recorta el usuario porque algunos navegadores/autocompletados
      // insertan espacios que el backend no tolera.
      await this.auth.login({ username: username.trim(), password });
      await this.router.navigate(['/dashboard'], { replaceUrl: true });
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'Usuario o contraseña incorrectos'));
    } finally {
      this.loading.set(false);
    }
  }
}
