import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { EmployeeDialog } from './employee-dialog';

/**
 * El botón "Crear" llama a submit() y, si el formulario estaba incompleto, no
 * pasaba nada visible: el usuario creía que el botón estaba roto. Estas pruebas
 * fijan ese comportamiento.
 */
describe('EmployeeDialog', () => {
  function setup(inicial: unknown = null) {
    const ref = { close: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: { initial: inicial } },
      ],
    });

    return { dialog: TestBed.runInInjectionContext(() => new EmployeeDialog()), ref };
  }

  it('se crea', () => {
    const { dialog } = setup();
    expect(dialog).toBeTruthy();
  });

  it('con el formulario vacío no cierra el diálogo y avisa qué falta', () => {
    const { dialog, ref } = setup();

    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
    expect(dialog.intentoFallido()).toBe(true);
    expect(dialog.camposFaltantes).toContain('el RUT');
    expect(dialog.camposFaltantes).toContain('la fecha de ingreso');
    expect(dialog.camposFaltantes).toContain('el nombre');
    expect(dialog.camposFaltantes).toContain('el apellido');
    expect(dialog.camposFaltantes).toContain('el sueldo base');
  });

  it('con el RUT vacío el campo muestra su error de obligatorio', () => {
    const { dialog } = setup();

    expect(dialog.rutVacio).toBe(true);
    expect(dialog.mostrarErrorRut).toBe(false);
    dialog.submit();
    // El error de obligatorio solo aparece tras tocar el campo.
    expect(dialog.rutTocado).toBe(true);
  });

  it('rechaza un RUT con dígito verificador incorrecto', () => {
    const { dialog, ref } = setup();

    dialog.form.patchValue({
      // El DV de 12.345.678 es 5, no 9.
      rut: '12.345.678-9',
      hireDate: '2026-09-28',
      firstName: 'Ana',
      lastName: 'Pérez',
      baseSalary: '530.000',
    });
    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
    expect(dialog.camposFaltantes.some((f) => f.includes('RUT válido'))).toBe(true);
  });

  it('con los datos completos cierra el diálogo con el empleado creado', () => {
    const { dialog, ref } = setup();

    dialog.form.patchValue({
      rut: '12.345.678-5',
      hireDate: '2026-09-28',
      firstName: 'Ana',
      lastName: 'Pérez',
      phone: '',
      email: '',
      baseSalary: '530.000',
      dependents: 0,
    });
    dialog.submit();

    expect(ref.close).toHaveBeenCalledTimes(1);
    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['rut']).toBe('12.345.678-5');
    expect(enviado['baseSalary']).toBe(530000);
    expect(enviado['firstName']).toBe('Ana');
    expect(enviado['phone']).toBeNull();
    expect(enviado['email']).toBeNull();
    expect(dialog.camposFaltantes).toEqual([]);
  });

  it('un segundo clic no crea el empleado dos veces', () => {
    const { dialog, ref } = setup();

    dialog.form.patchValue({
      rut: '12.345.678-5',
      hireDate: '2026-09-28',
      firstName: 'Ana',
      lastName: 'Pérez',
      baseSalary: '530.000',
    });
    dialog.submit();
    dialog.submit();

    expect(ref.close).toHaveBeenCalledTimes(1);
  });
});

describe('EmployeeDialog: dígito verificador esperado', () => {
  function setup() {
    const ref = { close: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: { initial: null } },
      ],
    });
    return TestBed.runInInjectionContext(() => new EmployeeDialog());
  }

  it('indica el DV correcto cuando el RUT está mal escrito', () => {
    const d = setup();
    d.form.controls.rut.setValue('12.345.678-9');
    expect(d.dvEsperado).toBe('5');
  });

  it('no sugiere DV si el RUT está incompleto', () => {
    const d = setup();
    d.form.controls.rut.setValue('12.345');
    expect(d.dvEsperado).toBeNull();
  });

  it('sugiere K cuando corresponde', () => {
    const d = setup();
    d.form.controls.rut.setValue('21.365.487-3');
    expect(d.dvEsperado).toBe('K');
  });
});

describe('EmployeeDialog: fondo AFP', () => {
  function setup(inicial: unknown = null) {
    const ref = { close: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: { initial: inicial } },
      ],
    });
    return { dialog: TestBed.runInInjectionContext(() => new EmployeeDialog()), ref };
  }

  const datosValidos = {
    rut: '10.561.117-K',
    hireDate: '2026-09-28',
    firstName: 'Ana',
    lastName: 'Pérez',
    baseSalary: '530.000',
  };

  it('ofrece solo los seis fondos de AFP', () => {
    const { dialog } = setup();
    expect(dialog.fondosAfp).toEqual([
      'AFP CUPRUM',
      'AFP HABITAT',
      'AFP MODELO',
      'AFP PLANVITAL',
      'AFP PROVIDA',
      'AFP UNO',
    ]);
  });

  it('guarda el fondo elegido con AFP', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...datosValidos, pensionSystem: 'AFP', afpFund: 'AFP CUPRUM' });
    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['afpFund']).toBe('AFP CUPRUM');
  });

  it('no manda fondo de AFP cuando el sistema es IPS', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...datosValidos, pensionSystem: 'IPS', afpFund: 'CUPRUM' });
    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['pensionSystem']).toBe('IPS');
    expect(enviado['afpFund']).toBeNull();
  });

  it('omite el fondo si el usuario elige AFP y no selecciona ninguno', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...datosValidos, pensionSystem: 'AFP', afpFund: '' });
    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['afpFund']).toBeNull();
  });
});
