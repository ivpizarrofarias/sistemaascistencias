import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

/** Diálogo de confirmación reutilizable para acciones destructivas. */
@Component({
  selector: 'app-confirm-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>
      <mat-icon [class.danger]="data.danger">
        {{ data.danger ? 'warning' : 'help' }}
      </mat-icon>
      {{ data.title }}
    </h2>

    <mat-dialog-content>{{ data.message }}</mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="ref.close(false)">{{ data.cancelText ?? 'Cancelar' }}</button>
      <button
        mat-flat-button
        [color]="data.danger ? 'warn' : 'primary'"
        (click)="ref.close(true)"
      >
        {{ data.confirmText ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    h2 {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    h2 .danger {
      color: var(--mat-sys-error);
    }
  `,
})
export class ConfirmDialog {
  readonly ref = inject(MatDialogRef<ConfirmDialog>);
  readonly data = inject<ConfirmData>(MAT_DIALOG_DATA);
}
