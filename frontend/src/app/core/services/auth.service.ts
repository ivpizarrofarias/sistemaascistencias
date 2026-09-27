import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { AuthResponse, LoginRequest, Role } from '../models/api.models';
import { ApiService } from './api.service';
import { AuthStorage } from './auth-storage';

/**
 * Estado de sesión de la aplicación.
 *
 * Solo el ADMIN inicia sesión: los empleados marcan asistencia con su código
 * en el kiosco, no con usuario y contraseña.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly storage = inject(AuthStorage);
  private readonly router = inject(Router);

  private readonly _user = signal<AuthResponse | null>(this.storage.getUser<AuthResponse>());

  /** Usuario actual (null si no hay sesión). */
  readonly user = this._user.asReadonly();

  readonly isAuthenticated = computed(() => Boolean(this._user()?.token));

  readonly role = computed<Role | null>(() => this._user()?.role ?? null);

  readonly employeeId = computed<number | null>(() => this._user()?.employeeId ?? null);

  /** Nombre a mostrar en el toolbar. */
  readonly displayName = computed(() => {
    const u = this._user();
    return u?.fullName ?? u?.username ?? '';
  });

  async login(payload: LoginRequest): Promise<AuthResponse> {
    const res = await firstValueFrom(this.api.login(payload));
    this.storage.setToken(res.token);
    this.storage.setUser(res);
    this._user.set(res);
    return res;
  }

  logout(): void {
    this.storage.clear();
    this._user.set(null);
    void this.router.navigate(['/login']);
  }
}

/**
 * Extrae un mensaje legible desde un error del backend.
 *
 * Cubre tres casos: error de validación con `fieldErrors`, errores de negocio
 * con `message`, y caída de red (sin `response`).
 */
export function apiErrorMessage(err: unknown, fallback = 'Ocurrió un error'): string {
  if (err instanceof HttpErrorResponse) {
    if (!err.error) {
      return 'No se pudo conectar con el servidor. Verifica que el backend esté corriendo en el puerto 8080.';
    }
    const body = err.error as {
      message?: string;
      fieldErrors?: Record<string, string>;
    };

    const fields = body?.fieldErrors;
    if (fields && Object.keys(fields).length > 0) {
      return Object.values(fields).join(' · ');
    }
    if (body?.message) return body.message;
    return err.statusText || fallback;
  }

  if (err instanceof Error && err.message) return err.message;
  return fallback;
}
