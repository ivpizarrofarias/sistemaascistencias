import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

/**
 * Rutas de la aplicación.
 *
 * Todo lo que cuelga de `shell` comparte el layout (sidenav + toolbar) y exige
 * sesión activa. Las secciones administrativas exigen además rol ADMIN.
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.LoginPage),
  },
  {
    path: '',
    canActivate: [authGuard()],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        title: 'Dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.DashboardPage),
      },
      {
        path: 'empleados',
        title: 'Empleados',
        canActivate: [authGuard(['ADMIN'])],
        loadComponent: () => import('./features/employees/employees').then((m) => m.EmployeesPage),
      },
      {
        path: 'horarios',
        title: 'Horarios',
        canActivate: [authGuard(['ADMIN'])],
        loadComponent: () => import('./features/schedules/schedules').then((m) => m.SchedulesPage),
      },
      {
        path: 'asistencias',
        title: 'Asistencias',
        canActivate: [authGuard(['ADMIN'])],
        loadComponent: () =>
          import('./features/attendance/attendance').then((m) => m.AttendancePage),
      },
      {
        path: 'permisos',
        title: 'Permisos',
        canActivate: [authGuard(['ADMIN'])],
        loadComponent: () => import('./features/permisos/permisos').then((m) => m.PermisosPage),
      },
      {
        path: 'remuneraciones',
        title: 'Remuneraciones',
        canActivate: [authGuard(['ADMIN'])],
        loadComponent: () => import('./features/payroll/payroll').then((m) => m.PayrollPage),
      },
      {
        path: 'no-autorizado',
        title: 'Acceso no autorizado',
        loadComponent: () =>
          import('./features/no-autorizado/no-autorizado').then((m) => m.NoAutorizadoPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
