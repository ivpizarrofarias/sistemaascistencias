import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavContainer, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';

import { AuthService } from '../core/services/auth.service';
import { Role } from '../core/models/api.models';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  roles?: Role[];
}

/** Secciones del menú lateral. `roles` vacío = visible para todos los autenticados. */
const NAV_ITEMS: NavItem[] = [
  { path: '/dashboard', label: 'Dashboard', icon: 'space_dashboard' },
  { path: '/empleados', label: 'Empleados', icon: 'group' },
  { path: '/horarios', label: 'Horarios', icon: 'calendar_month' },
  { path: '/asistencias', label: 'Asistencias', icon: 'how_to_reg' },
  { path: '/permisos', label: 'Permisos', icon: 'description' },
  { path: '/remuneraciones', label: 'Remuneraciones', icon: 'payments' },
];

@Component({
  selector: 'app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatDividerModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly auth = inject(AuthService);
  private readonly breakpointObserver = inject(BreakpointObserver);

  readonly navItems = NAV_ITEMS;

  private readonly container = viewChild.required<MatSidenavContainer>('container');

  /** Teléfono / tablet: el menú se oculta y se abre como overlay. */
  readonly isMobile = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.Handset, Breakpoints.TabletPortrait])
      .pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  /** Escritorio: el menú se puede colapsar a solo iconos. */
  readonly collapsed = signal(false);

  /** En móvil el sidenav arranca cerrado; en escritorio, abierto. */
  readonly opened = signal(true);

  readonly displayName = this.auth.displayName;
  readonly role = this.auth.role;

  readonly visibleItems = computed(() => {
    const role = this.role();
    return this.navItems.filter((i) => !i.roles || (role && i.roles.includes(role)));
  });

  readonly roleLabel = computed(() => (this.role() === 'ADMIN' ? 'Administrador' : 'Empleado'));

  toggle(): void {
    if (this.isMobile()) {
      this.opened.update((v) => !v);
    } else {
      this.collapsed.update((v) => !v);
    }
  }

  /**
   * El ancho del sidenav se anima por CSS, así que el contenedor puede quedar
   * con el margen del contenido desfasado al terminar la transición.
   */
  onSidenavTransitionEnd(event: TransitionEvent): void {
    if (event.target !== event.currentTarget && !this.isMobile()) {
      this.container().updateContentMargins();
    }
  }

  closeOnMobile(): void {
    if (this.isMobile()) this.opened.set(false);
  }

  logout(): void {
    this.auth.logout();
  }
}
