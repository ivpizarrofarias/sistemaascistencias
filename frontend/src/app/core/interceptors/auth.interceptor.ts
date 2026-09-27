import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuthStorage } from '../services/auth-storage';

/**
 * Intercepta todas las peticiones HTTP:
 * - adjunta el token JWT como `Authorization: Bearer <token>`;
 * - si el backend responde 401, limpia la sesión y manda al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(AuthStorage);
  const router = inject(Router);

  const token = storage.getToken();
  const cloned = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(cloned).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401) {
        storage.clear();
        if (!router.url.startsWith('/login')) {
          void router.navigate(['/login']);
        }
      }
      return throwError(() => err);
    }),
  );
};
