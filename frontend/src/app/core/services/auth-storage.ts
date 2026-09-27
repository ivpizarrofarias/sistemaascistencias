import { Injectable } from '@angular/core';

/** Llaves de las claves usadas en `localStorage`. */
const TOKEN_KEY = 'sistema_asistencias_token';
const USER_KEY = 'sistema_asistencias_user';

/**
 * Persistencia de la sesión en `localStorage`.
 *
 * Se mantiene fuera de Angular para que el interceptor de HTTP pueda leer el
 * token de forma sincrónica sin inyectar dependencias.
 */
@Injectable({ providedIn: 'root' })
export class AuthStorage {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getUser<T>(): T | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as T) : null;
    } catch {
      return null;
    }
  }

  setUser(user: unknown): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}
