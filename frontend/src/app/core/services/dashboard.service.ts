import { Injectable, inject, signal } from '@angular/core';
import { Observable, firstValueFrom, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  AttendanceResponse,
  EmployeeResponse,
  HoraExtraResponse,
  PermisoEstado,
  PermisoLaboralResponse,
} from '../models/api.models';
import { ApiService } from './api.service';
import { apiErrorMessage } from './auth.service';
import { firstDayOfMonthIso, lastDayOfMonthIso } from '../utils/format';

/** Punto de la serie diaria de asistencia. */
export interface DailyPoint {
  /** Etiqueta corta del eje X, p. ej. "01/09". */
  label: string
  date: string
  presentes: number
  inasistencias: number
  /** Porcentaje de asistencia del día (0-100). */
  porcentaje: number
}

/** Resumen de asistencia de un empleado en el período. */
export interface EmployeeAttendance {
  employee: EmployeeResponse
  presentes: number
  inasistencias: number
  licencias: number
  /** Porcentaje de asistencia (0-100). */
  porcentaje: number
}

/** Reparto de estados en el período, para el gráfico de anillo. */
export interface AttendanceSplit {
  presentes: number
  inasistencias: number
  licencias: number
}

/** Todo lo que el dashboard necesita en una sola carga. */
export interface DashboardSnapshot {
  empleadosActivos: number
  totalEmpleados: number
  presentes: number
  inasistencias: number
  licencias: number
  /** Porcentaje de asistencia del período (0-100). */
  tasaAsistencia: number
  permisosPendientes: number
  horasExtraMonto: number
  horasExtraRegistros: number
  diasConRegistro: number
  daily: DailyPoint[]
  porEmpleado: EmployeeAttendance[]
  split: AttendanceSplit
}

/** Fila de la consulta por empleado que se ejecuta en paralelo. */
interface EmployeeBundle {
  empleado: EmployeeResponse
  asistencias: AttendanceResponse[]
  horasExtras: HoraExtraResponse[]
}

/**
 * Agrega los datos de varios endpoints para el dashboard.
 *
 * El backend no expone un endpoint de estadísticas, así que el resumen se arma
 * en el cliente: se listan los empleados y luego, en paralelo, se piden las
 * asistencias y horas extra de cada uno dentro del mes (dos peticiones por
 * empleado). Un fallo puntual de un empleado no tumba el dashboard completo.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);

  private year = new Date().getFullYear();
  private month = new Date().getMonth() + 1;

  private readonly _loading = signal(false);
  private readonly _error = signal('');
  private readonly _snapshot = signal<DashboardSnapshot | null>(null);
  private readonly _periodLabel = signal('');

  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly snapshot = this._snapshot.asReadonly();
  readonly periodLabel = this._periodLabel.asReadonly();

  /** Año y mes actualmente cargados. */
  readonly period = signal({ year: this.year, month: this.month });

  /** Carga (o recarga) los datos del mes indicado. */
  async load(year = this.year, month = this.month): Promise<void> {
    this.year = year;
    this.month = month;
    this.period.set({ year, month });

    this._loading.set(true);
    this._error.set('');
    this._periodLabel.set(
      new Date(year, month - 1, 1).toLocaleDateString('es-CL', {
        month: 'long',
        year: 'numeric',
      }),
    );

    const desde = firstDayOfMonthIso(year, month);
    const hasta = lastDayOfMonthIso(year, month);

    try {
      const base = await firstValueFrom(
        forkJoin({
          empleados: this.api.listEmployees().pipe(catchError(() => of([] as EmployeeResponse[]))),
          permisos: this.api
            .listPermisos()
            .pipe(catchError(() => of([] as PermisoLaboralResponse[]))),
        }),
      );

      const pendientes = this.countPendientes(base.permisos);

      // Un `Observable` por empleado; si uno falla, se degrada a lista vacía
      // en vez de tumbar el dashboard completo.
      const porEmpleado: Observable<EmployeeBundle>[] = base.empleados.map(
        (empleado: EmployeeResponse) =>
          forkJoin({
            empleado: of(empleado),
            asistencias: this.api
              .listAttendance(empleado.id, desde, hasta)
              .pipe(catchError(() => of([] as AttendanceResponse[]))),
            horasExtras: this.api
              .listHorasExtras(empleado.id)
              .pipe(catchError(() => of([] as HoraExtraResponse[]))),
          }),
      );

      const rows: EmployeeBundle[] =
        porEmpleado.length > 0
          ? await firstValueFrom(forkJoin(porEmpleado))
          : [];

      this._loading.set(false);
      this._snapshot.set(this.build(rows, pendientes));
    } catch (err: unknown) {
      this._loading.set(false);
      this._error.set(apiErrorMessage(err, 'No se pudo cargar el dashboard'));
    }
  }

  private countPendientes(permisos: PermisoLaboralResponse[]): number {
    return permisos.filter((p) => p.estado === ('PENDIENTE' satisfies PermisoEstado)).length;
  }

  private build(rows: EmployeeBundle[], permisosPendientes: number): DashboardSnapshot {
    let presentes = 0;
    let inasistencias = 0;
    let licencias = 0;
    let horasExtraMonto = 0;
    let horasExtraRegistros = 0;

    const porEmpleado: EmployeeAttendance[] = [];
    /** Acumulador por fecha: `yyyy-MM-dd` -> conteos del día. */
    const porFecha = new Map<string, { presentes: number; inasistencias: number }>();

    const desde = firstDayOfMonthIso(this.year, this.month);
    const hasta = lastDayOfMonthIso(this.year, this.month);

    for (const row of rows) {
      const pres = row.asistencias.filter((a) => !a.absence).length;
      const inas = row.asistencias.filter((a) => a.absence).length;
      const lic = row.asistencias.filter((a) => a.medicalLicense).length;

      // Las horas extra se atribuyen al mes de la fecha del registro.
      for (const he of row.horasExtras) {
        if (he.date >= desde && he.date <= hasta) {
          horasExtraMonto += he.monto ?? 0;
          horasExtraRegistros += 1;
        }
      }

      presentes += pres;
      inasistencias += inas;
      licencias += lic;

      const denom = pres + inas;
      porEmpleado.push({
        employee: row.empleado,
        presentes: pres,
        inasistencias: inas,
        licencias: lic,
        porcentaje: denom === 0 ? 0 : (pres / denom) * 100,
      });

      for (const a of row.asistencias) {
        const acc = porFecha.get(a.date) ?? { presentes: 0, inasistencias: 0 };
        if (a.absence) acc.inasistencias += 1;
        else acc.presentes += 1;
        porFecha.set(a.date, acc);
      }
    }

    const daily: DailyPoint[] = [...porFecha.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, v]) => {
        const total = v.presentes + v.inasistencias;
        return {
          label: `${date.slice(8, 10)}/${date.slice(5, 7)}`,
          date,
          presentes: v.presentes,
          inasistencias: v.inasistencias,
          porcentaje: total === 0 ? 0 : (v.presentes / total) * 100,
        };
      });

    const denomTotal = presentes + inasistencias;

    return {
      empleadosActivos: rows.filter((r) => r.empleado.enabled).length,
      totalEmpleados: rows.length,
      presentes,
      inasistencias,
      licencias,
      tasaAsistencia: denomTotal === 0 ? 0 : (presentes / denomTotal) * 100,
      permisosPendientes,
      horasExtraMonto,
      horasExtraRegistros,
      diasConRegistro: daily.length,
      daily,
      // Menor porcentaje primero: highlights arriba en la tabla.
      porEmpleado: [...porEmpleado].sort((a, b) => a.porcentaje - b.porcentaje),
      split: { presentes, inasistencias, licencias },
    };
  }
}
