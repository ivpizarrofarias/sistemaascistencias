import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { EmployeeResponse } from '../../core/models/api.models';
import { PermisoDialog } from './permiso-dialog';

/**
 * El campo que no aplica al tipo de permiso elegido debe mandarse en `null`, no
 * en 0. El backend exige mínimo 1 día, así que mandar 0 rechazaba el alta entera
 * con un HTTP 400 y el usuario veía que "Guardar permiso" no hacía nada.
 */
describe('PermisoDialog', () => {
  const empleados = [
    { id: 3, fullName: 'Javiera Muñoz', rut: '16.876.543-6' },
  ] as EmployeeResponse[];

  function setup() {
    const ref = { close: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: { employees: empleados } },
      ],
    });
    return { dialog: TestBed.runInInjectionContext(() => new PermisoDialog()), ref };
  }

  const base = {
    employeeId: 3,
    date: '2026-09-28',
    motivo: 'Trámite personal',
  };

  it('manda días en null cuando el permiso es por hora', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, tipo: 'POR_HORA', horas: 2 });

    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['tipo']).toBe('POR_HORA');
    expect(enviado['horas']).toBe(2);
    expect(enviado['dias']).toBeNull();
  });

  it('manda horas en null cuando el permiso es por día', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, tipo: 'POR_DIA', dias: 2 });

    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['tipo']).toBe('POR_DIA');
    expect(enviado['dias']).toBe(2);
    expect(enviado['horas']).toBeNull();
  });

  it('nunca manda 0 en días, que el backend rechaza', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, tipo: 'POR_HORA', horas: 1 });

    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['dias']).not.toBe(0);
  });

  it('no cierra si falta el empleado', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, employeeId: 0, tipo: 'POR_HORA', horas: 2 });

    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
  });

  it('no cierra si falta la fecha', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, date: '', tipo: 'POR_HORA', horas: 2 });

    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
  });

  it('no cierra si falta el motivo', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, motivo: '', tipo: 'POR_HORA', horas: 2 });

    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
  });

  it('guarda un permiso por día aunque las horas queden en 0', () => {
    // El campo "horas" arranca en 0 y no se ve con POR_DIA. Con un validador
    // fijo de mínimo 0.01 el formulario quedaba inválido siempre y el botón
    // "Guardar permiso" no cerraba el diálogo.
    const { dialog, ref } = setup();
    expect(dialog.form.controls.horas.value).toBe(0);

    dialog.form.patchValue({ ...base, tipo: 'POR_DIA', dias: 2 });
    // Lo único que puede invalidar el formulario es la cantidad, y aquí no aplica.
    expect(dialog.form.invalid).toBe(false);
    dialog.submit();

    expect(ref.close).toHaveBeenCalledTimes(1);
  });

  it('rechaza un permiso por hora con 0 horas', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, tipo: 'POR_HORA', horas: 0 });

    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
  });

  it('avisa en pantalla cuando la cantidad queda vacía', () => {
    // El input numérico vacío llega como "" (string). Compararlo sin
    // normalizar bloqueaba el guardado sin mostrar ningún error.
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, tipo: 'POR_HORA' });
    dialog.form.controls.horas.setValue('' as unknown as number);

    dialog.submit();

    expect(ref.close).not.toHaveBeenCalled();
    expect(dialog.form.controls.horas.hasError('horasInvalidas')).toBe(true);
  });

  it('guarda cuando las horas vienen como texto numérico', () => {
    const { dialog, ref } = setup();
    dialog.form.patchValue({ ...base, tipo: 'POR_HORA' });
    // Así es exactamente como llega del input type="number".
    dialog.form.controls.horas.setValue('2' as unknown as number);

    dialog.submit();

    const enviado = ref.close.mock.calls[0][0] as Record<string, unknown>;
    expect(enviado['horas']).toBe(2);
  });

  it('previsualiza las fechas cuando el permiso es por día', () => {
    const { dialog } = setup();
    dialog.onTipoChange('POR_DIA');
    dialog.onDateChange('2026-09-28');
    dialog.onDiasChange('3');

    expect(dialog.fechas()).toEqual(['2026-09-28', '2026-09-29', '2026-09-30']);
  });
});
