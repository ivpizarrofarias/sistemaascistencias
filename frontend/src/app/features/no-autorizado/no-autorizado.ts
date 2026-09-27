import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-autorizado',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatIconModule, RouterLink],
  template: `
    <div class="na-wrap">
      <mat-icon class="na-icon">lock</mat-icon>
      <h2>Acceso no autorizado</h2>
      <p>No tienes permisos para ver esta sección.</p>
      <a mat-flat-button color="primary" routerLink="/dashboard">
        <mat-icon>arrow_back</mat-icon>
        Volver al dashboard
      </a>
    </div>
  `,
  styles: `
    .na-wrap {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 96px 24px;
      text-align: center;
    }

    .na-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: var(--mat-sys-outline);
    }

    h2 {
      margin: 12px 0 0;
      font-size: 1.5rem;
      font-weight: 500;
    }

    p {
      margin: 0 0 20px;
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class NoAutorizadoPage {}
