import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type StatTone = 'primary' | 'success' | 'warning' | 'error' | 'neutral';

/** Tarjeta de indicador (KPI) para el dashboard. */
@Component({
  selector: 'app-stat-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatIconModule],
  template: `
    <div class="stat" [attr.data-tone]="tone()">
      <div class="stat-icon">
        <mat-icon>{{ icon() }}</mat-icon>
      </div>
      <div class="stat-body">
        <span class="stat-label">{{ label() }}</span>
        <span class="stat-value">{{ value() }}</span>
        @if (hint()) {
          <span class="stat-hint">{{ hint() }}</span>
        }
      </div>
    </div>
  `,
  styles: `
    .stat {
      display: flex;
      align-items: center;
      gap: 16px;
      height: 100%;
      padding: 20px;
      border-radius: 16px;
      background: var(--mat-sys-surface);
      border: 1px solid var(--mat-sys-outline-variant);
      box-shadow: 0 1px 2px rgb(0 0 0 / 4%);
    }

    .stat-icon {
      display: grid;
      place-items: center;
      width: 48px;
      height: 48px;
      flex-shrink: 0;
      border-radius: 12px;
    }

    .stat-icon mat-icon {
      font-size: 26px;
      width: 26px;
      height: 26px;
    }

    [data-tone='primary'] .stat-icon {
      background: var(--mat-sys-primary-container);
      color: var(--mat-sys-on-primary-container);
    }

    [data-tone='success'] .stat-icon {
      background: #e3f7e8;
      color: #1b5e20;
    }

    [data-tone='warning'] .stat-icon {
      background: #fff3d6;
      color: #7a4f01;
    }

    [data-tone='error'] .stat-icon {
      background: var(--mat-sys-error-container);
      color: var(--mat-sys-on-error-container);
    }

    [data-tone='neutral'] .stat-icon {
      background: var(--mat-sys-surface-container-highest);
      color: var(--mat-sys-on-surface-variant);
    }

    .stat-body {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .stat-label {
      font-size: 0.8rem;
      color: var(--mat-sys-on-surface-variant);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .stat-value {
      font-size: 1.6rem;
      font-weight: 600;
      line-height: 1.2;
      color: var(--mat-sys-on-surface);
      font-variant-numeric: tabular-nums;
    }

    .stat-hint {
      font-size: 0.78rem;
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class StatCard {
  readonly icon = input.required<string>();
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  readonly hint = input<string>('');
  readonly tone = input<StatTone>('primary');
}
