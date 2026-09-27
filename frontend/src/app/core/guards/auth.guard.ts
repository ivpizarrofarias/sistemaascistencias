import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Role } from '../models/api.models';
import { AuthService } from '../services/auth.service';

/**
 * Guarda de rutas protegidas.
 *
 * - Sin `roles`: basta con estar autenticado.
 * - Con `roles`: además debe coincidir el rol, si no va a `/no-autorizado`.
 */
export function authGuard(roles?: Role[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    if (roles && roles.length > 0) {
      const role = auth.role();
      if (!role || !roles.includes(role)) {
        return router.createUrlTree(['/no-autorizado']);
      }
    }

    return true;
  };
}
