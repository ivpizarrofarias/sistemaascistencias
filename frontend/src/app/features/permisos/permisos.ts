import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';
import { map } from 'rxjs/operators';

import {
  DevolucionRequest,
  EmployeeResponse,
  FORMA_COMPENSACION_LABEL,
  PERMISO_ESTADO_LABEL,
  PERMISO_TIPO_LABEL,
  PermisoEstado,
  PermisoLaboralResponse,
} from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import { formatDate } from '../../core/utils/format';
import { PageHeader } from '../../shared/page-header';
import { EmptyState, Loading } from '../../shared/states';
import { ConfirmDialog, ConfirmData } from '../../shared/confirm-dialog';
import { PermisoDialog, PermisoFormResult } from './permiso-dialog';
import { DevolucionDialog, DevolucionData } from './devolucion-dialog';

@Component({
  selector: 'app-permisos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    PageHeader,
    EmptyState,
    Loading,
  ],
  templateUrl: './permisos.html',
  styleUrl: './permisos.scss',
})
export class PermisosPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly permisos = signal<PermisoLaboralResponse[]>([]);
  readonly employees = signal<EmployeeResponse[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');

  readonly formatDate = formatDate;

  readonly columns = [
    'empleado',
    'fecha',
    'tipo',
    'motivo',
    'compensacion',
    'estado',
    'acciones',
  ];

  /** Contadores por estado, para el resumen superior. */
  readonly resumen = computed(() => {
    const rows = this.permisos();
    return {
      total: rows.length,
      pendientes: rows.filter((p) => p.estado === 'PENDIENTE').length,
      aprobados: rows.filter((p) => p.estado === 'APROBADO').length,
      rechazados: rows.filter((p) => p.estado === 'RECHAZADO').length,
    };
  });

  ngOnInit(): void {
    void this.load();
    void this.loadEmployees();
  }

  // Las filas de `mat-table` llegan tipadas como `any`; estos helpers mantienen
  // el acceso a los catálogos strongly-typed dentro de la plantilla.
  tipoDe(p: PermisoLaboralResponse): string {
    return PERMISO_TIPO_LABEL[p.tipo];
  }

  estadoDe(p: PermisoLaboralResponse): string {
    return PERMISO_ESTADO_LABEL[p.estado];
  }

  compensacionDe(p: PermisoLaboralResponse): string {
    return FORMA_COMPENSACION_LABEL[p.formaCompensacion];
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      this.permisos.set(await firstValueFrom(this.api.listPermisos()));
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudieron cargar los permisos'));
    } finally {
      this.loading.set(false);
    }
  }

  private async loadEmployees(): Promise<void> {
    try {
      this.employees.set(await firstValueFrom(this.api.listEmployees()));
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudieron cargar los empleados'));
    }
  }

  async openCreate(): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<PermisoDialog, { employees: EmployeeResponse[] }, PermisoFormResult | undefined>(
          PermisoDialog,
          { data: { employees: this.employees() }, width: '560px', maxWidth: '95vw' },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () =>
        firstValueFrom(
          this.api.createPermisoAsAdmin(result.employeeId, {
            date: result.date,
            tipo: result.tipo,
            horas: result.horas,
            dias: result.dias,
            motivo: result.motivo,
            formaCompensacion: result.formaCompensacion,
          }),
        ),
      'Permiso creado y aprobado',
    );
  }

  async validate(p: PermisoLaboralResponse, estado: PermisoEstado): Promise<void> {
    const data: ConfirmData = {
      title: estado === 'APROBADO' ? 'Aprobar permiso' : 'Rechazar permiso',
      message: `¿Marcar el permiso de ${p.employeeName} del ${formatDate(
        p.date,
      )} como ${PERMISO_ESTADO_LABEL[estado]}?`,
      confirmText: PERMISO_ESTADO_LABEL[estado],
      danger: estado === 'RECHAZADO',
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(
      () => firstValueFrom(this.api.validatePermiso(p.id, { estado })),
      `Permiso ${PERMISO_ESTADO_LABEL[estado].toLowerCase()}`,
    );
  }

  async remove(p: PermisoLaboralResponse): Promise<void> {
    const data: ConfirmData = {
      title: 'Eliminar permiso',
      message: `¿Eliminar el permiso #${p.id} de ${p.employeeName}?`,
      confirmText: 'Eliminar',
      danger: true,
    };
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    if (!(await firstValueFrom(ref.afterClosed().pipe(map((r) => r === true))))) return;

    await this.run(() => firstValueFrom(this.api.deletePermiso(p.id)), 'Permiso eliminado');
  }

  /** Registra el tiempo que el trabajador ya devolvió (para que no se descuente). */
  async openDevolucion(p: PermisoLaboralResponse): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<DevolucionDialog, DevolucionData, DevolucionRequest | undefined>(DevolucionDialog, {
          data: { permiso: p },
          width: '520px',
          maxWidth: '95vw',
        })
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () => firstValueFrom(this.api.registerDevolucion(p.id, result)),
      'Devolución registrada',
    );
  }

  private async run(action: () => Promise<unknown>, successMessage: string): Promise<void> {
    this.saving.set(true);
    this.error.set('');
    try {
      await action();
      this.snackBar.open(successMessage, 'Cerrar', { duration: 3500 });
      await this.load();
    } catch (err: unknown) {
      const message = apiErrorMessage(err);
      this.error.set(message);
      this.snackBar.open(message, 'Cerrar', { duration: 6000 });
    } finally {
      this.saving.set(false);
    }
  }
}
