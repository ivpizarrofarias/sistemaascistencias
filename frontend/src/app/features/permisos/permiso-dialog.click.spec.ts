import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { EmployeeResponse } from '../../core/models/api.models';
import { PermisoDialog } from './permiso-dialog';

/**
 * Prueba del template real: monta el diálogo y hace clic en el botón
 * "Guardar permiso", que es lo que hace el usuario. Los tests anteriores
 * llamaban a submit() directamente y se saltaban el binding del botón.
 */
describe('PermisoDialog (clic en Guardar permiso)', () => {
  let fixture: ComponentFixture<PermisoDialog>;
  let ref: { close: ReturnType<typeof vi.fn> };
  let dialog: PermisoDialog;

  const empleados = [{ id: 3, fullName: 'Javiera Muñoz', rut: '16.876.543-6' }] as EmployeeResponse[];

  beforeEach(async () => {
    ref = { close: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [PermisoDialog],
      providers: [
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: { employees: empleados } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PermisoDialog);
    dialog = fixture.componentInstance;
    fixture.detectChanges();
  });

  function botonGuardar(): HTMLButtonElement {
    const botones = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const b = botones.find((x) => (x.textContent ?? '').includes('Guardar permiso'));
    if (!b) throw new Error('no se encontró el botón Guardar permiso');
    return b;
  }

  function llenar(horas: number | string) {
    dialog.form.patchValue({
      employeeId: 3,
      date: '2026-09-28',
      tipo: 'POR_HORA',
      motivo: 'Cita medica',
    });
    dialog.form.controls.horas.setValue(horas as number);
    fixture.detectChanges();
  }

  it('el botón existe y está habilitado', () => {
    const b = botonGuardar();
    expect(b).toBeTruthy();
    expect(b.disabled).toBe(false);
  });

  it('guarda y cierra el diálogo al hacer clic', () => {
    llenar(2);

    botonGuardar().click();
    fixture.detectChanges();

    expect(ref.close).toHaveBeenCalledTimes(1);
    const enviado = ref.close.mock.calls[0][0];
    expect(enviado.employeeId).toBe(3);
    expect(enviado.motivo).toBe('Cita medica');
    expect(enviado.horas).toBe(2);
    expect(enviado.dias).toBeNull();
  });

  it('muestra el error en pantalla y NO cierra si falta la cantidad', () => {
    llenar('');

    botonGuardar().click();
    fixture.detectChanges();

    expect(ref.close).not.toHaveBeenCalled();
    const texto = fixture.nativeElement.textContent as string;
    expect(texto).toContain('Ingrese una cantidad de horas mayor a 0');
  });

  it('avisa si no se eligió empleado', () => {
    llenar(2);
    dialog.form.controls.employeeId.setValue(0);
    fixture.detectChanges();

    botonGuardar().click();
    fixture.detectChanges();

    expect(ref.close).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Seleccione un empleado');
  });
});
