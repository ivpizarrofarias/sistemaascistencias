import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';

import { SystemUser, SystemUserRequest } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import { NotifyService } from '../../core/services/notify.service';
import { PageHeader } from '../../shared/page-header';
import { ConfirmDialog, ConfirmData } from '../../shared/confirm-dialog';
import { EmptyState, Loading } from '../../shared/states';
import { UserDialog } from './user-dialog';

@Component({
  selector: 'app-users',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule,
    PageHeader,
    EmptyState,
    Loading,
  ],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class UsersPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotifyService);

  readonly users = signal<SystemUser[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly columns = ['usuario', 'nombre', 'rol', 'estado', 'acciones'];

  readonly total = computed(() => this.users().length);

  ngOnInit(): void {
    void this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      this.users.set(await firstValueFrom(this.api.listUsers()));
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudieron cargar los usuarios'));
    } finally {
      this.loading.set(false);
    }
  }

  roleLabel(role: string): string {
    return role === 'ADMIN' ? 'Administrador' : 'Supervisor';
  }

  async openCreate(): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open(UserDialog, { data: { initial: null }, width: '560px', maxWidth: '94vw' })
        .afterClosed(),
    );
    if (!result) return;
    await this.run(() => firstValueFrom(this.api.createUser(result as SystemUserRequest)), 'Usuario creado');
  }

  async openEdit(user: SystemUser): Promise<void> {
    const result = await firstValueFrom(
      this.dialog
        .open(UserDialog, { data: { initial: user }, width: '560px', maxWidth: '94vw' })
        .afterClosed(),
    );
    if (!result) return;
    await this.run(() => firstValueFrom(this.api.updateUser(user.id, result as SystemUserRequest)), 'Usuario actualizado');
  }

  async toggleEnabled(user: SystemUser): Promise<void> {
    const enabling = !user.enabled;
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        title: enabling ? 'Habilitar usuario' : 'Deshabilitar usuario',
        message: `${enabling ? '¿Habilitar' : '¿Deshabilitar'} a ${user.username}?`,
        confirmText: enabling ? 'Habilitar' : 'Deshabilitar',
        danger: !enabling,
      } satisfies ConfirmData,
      width: '420px',
    });
    const ok = await firstValueFrom(ref.afterClosed());
    if (!ok) return;
    await this.run(() => firstValueFrom(this.api.setUserEnabled(user.id, enabling)), enabling ? 'Usuario habilitado' : 'Usuario deshabilitado');
  }

  async remove(user: SystemUser): Promise<void> {
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        title: 'Eliminar usuario',
        message: `¿Eliminar al usuario ${user.username}? Esta acción no se puede deshacer.`,
        confirmText: 'Eliminar',
        danger: true,
      } satisfies ConfirmData,
      width: '420px',
    });
    const ok = await firstValueFrom(ref.afterClosed());
    if (!ok) return;
    await this.run(() => firstValueFrom(this.api.deleteUser(user.id)), 'Usuario eliminado');
  }

  private async run(action: () => Promise<unknown>, successMessage: string): Promise<void> {
    this.saving.set(true);
    try {
      await action();
      this.notify.success(successMessage);
      await this.load();
    } catch (err: unknown) {
      this.notify.error(apiErrorMessage(err, 'No se pudo completar la acción'));
    } finally {
      this.saving.set(false);
    }
  }
}
