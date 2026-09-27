import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';
import { map } from 'rxjs/operators';

import { EmployeeRequest, EmployeeResponse } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import { formatDate, formatMoney, normalizeText } from '../../core/utils/format';
import { PageHeader } from '../../shared/page-header';
import { EmptyState, Loading } from '../../shared/states';
import { ConfirmDialog, ConfirmData } from '../../shared/confirm-dialog';
import { EmployeeDialog } from './employee-dialog';

@Component({
  selector: 'app-employees',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    PageHeader,
    EmptyState,
    Loading,
  ],
  templateUrl: './employees.html',
  styleUrl: './employees.scss',
})
export class EmployeesPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly employees = signal<EmployeeResponse[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly search = signal('');

  readonly formatMoney = formatMoney;
  readonly formatDate = formatDate;

  readonly columns = ['rut', 'nombre', 'codigo', 'sueldo', 'cargas', 'estado', 'acciones'];

  /** Lista filtrada por texto libre (nombre, RUT, código o email). */
  readonly filtered = computed(() => {
    const q = normalizeText(this.search());
    const rows = this.employees();
    if (!q) return rows;
    return rows.filter((e) =>
      normalizeText(
        [e.fullName, e.rut, e.code, e.email ?? '', e.firstName, e.lastName].join(' '),
      ).includes(q),
    );
  });

  readonly totalActivos = computed(() => this.employees().filter((e) => e.enabled).length);

  ngOnInit(): void {
    void this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      this.employees.set(await firstValueFrom(this.api.listEmployees()));
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudieron cargar los empleados'));
    } finally {
      this.loading.set(false);
    }
  }

  onSearch(value: string): void {
    this.search.set(value);
  }

  async openCreate(): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<EmployeeDialog, { initial: EmployeeResponse | null }, EmployeeRequest>(
          EmployeeDialog,
          { data: { initial: null }, width: '760px', maxWidth: '95vw', autoFocus: 'first-tabbable' },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(() => firstValueFrom(this.api.createEmployee(result)), 'Empleado creado');
  }

  async openEdit(employee: EmployeeResponse): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open<EmployeeDialog, { initial: EmployeeResponse | null }, EmployeeRequest>(
          EmployeeDialog,
          {
            data: { initial: employee },
            width: '760px',
            maxWidth: '95vw',
            autoFocus: 'first-tabbable',
          },
        )
        .afterClosed(),
    );
    if (!result) return;

    await this.run(
      () => firstValueFrom(this.api.updateEmployee(employee.id, result)),
      'Empleado actualizado',
    );
  }

  async toggleEnabled(employee: EmployeeResponse): Promise<void> {
    const enabling = !employee.enabled;
    const ok = await this.confirm({
      title: enabling ? 'Habilitar empleado' : 'Deshabilitar empleado',
      message: `¿Seguro que deseas ${enabling ? 'habilitar' : 'deshabilitar'} a ${
        employee.fullName
      }?`,
      confirmText: enabling ? 'Habilitar' : 'Deshabilitar',
    });
    if (!ok) return;

    await this.run(
      () => firstValueFrom(this.api.setEmployeeEnabled(employee.id, enabling)),
      enabling ? 'Empleado habilitado' : 'Empleado deshabilitado',
    );
  }

  async remove(employee: EmployeeResponse): Promise<void> {
    const ok = await this.confirm({
      title: 'Eliminar empleado',
      message: `¿Realmente deseas eliminar a ${employee.fullName}? Esta acción no se puede deshacer.`,
      confirmText: 'Eliminar',
      danger: true,
    });
    if (!ok) return;

    await this.run(() => firstValueFrom(this.api.deleteEmployee(employee.id)), 'Empleado eliminado');
  }

  private confirm(data: ConfirmData): Promise<boolean> {
    const ref = this.dialog.open<ConfirmDialog, ConfirmData, boolean>(ConfirmDialog, { data });
    return firstValueFrom(ref.afterClosed().pipe(map((result) => result === true)));
  }

  /** Ejecuta una mutación, muestra el resultado y recarga la tabla. */
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
