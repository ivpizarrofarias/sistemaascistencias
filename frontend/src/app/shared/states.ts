import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/** Estado vacío con icono y mensaje. */
@Component({
  selector: 'app-empty-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatIconModule],
  template: `
    <div class="empty-state">
      <mat-icon>{{ icon() }}</mat-icon>
      <p>{{ message() }}</p>
      <ng-content />
    </div>
  `,
})
export class EmptyState {
  readonly icon = input<string>('inbox');
  readonly message = input.required<string>();
}

/** Indicador de carga inline. */
@Component({
  selector: 'app-loading',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatProgressBarModule, MatProgressSpinnerModule],
  template: `
    @if (spinner()) {
      <div class="loading-box">
        <mat-spinner [diameter]="40" />
        <span>{{ message() }}</span>
      </div>
    } @else {
      <mat-progress-bar mode="indeterminate" />
    }
  `,
  styles: `
    .loading-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 48px 24px;
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class Loading {
  readonly spinner = input<boolean>(false);
  readonly message = input<string>('Cargando…');
}
