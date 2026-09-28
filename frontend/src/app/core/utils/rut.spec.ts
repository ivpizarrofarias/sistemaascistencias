import { describe, expect, it } from 'vitest';

import {
  digitoVerificadorEsperado,
  formatearMiles,
  formatearRut,
  parsearMiles,
  rutCanónico,
  validarRut,
} from './rut';

/** Simula el tipeo real: cada carácter vuelve a pasar por `onRutInput`. */
function tipear(comoSeEscribe: string): string {
  let valor = '';
  for (const ch of comoSeEscribe) valor = formatearRut(valor + ch);
  return valor;
}

const soloDigitos = (s: string) => s.replace(/\D/g, '');

describe('digitoVerificadorEsperado', () => {
  it('usa la serie oficial del SII: 2,3,4,5,6,7 y vuelve a 2', () => {
    // Contrastados con los ejemplos publicados del algoritmo.
    expect(digitoVerificadorEsperado('30686957')).toBe('4');
    expect(digitoVerificadorEsperado('27962409')).toBe('2');
    expect(digitoVerificadorEsperado('13537201')).toBe('3');
    expect(digitoVerificadorEsperado('1091397')).toBe('7');
    expect(digitoVerificadorEsperado('11111111')).toBe('1');
    expect(digitoVerificadorEsperado('12345678')).toBe('5');
  });

  it('devuelve K cuando corresponde y 0 cuando el resto es 0', () => {
    expect(digitoVerificadorEsperado('21365487')).toBe('K');
    expect(digitoVerificadorEsperado('7600000')).toBe('K');
    expect(digitoVerificadorEsperado('10000004')).toBe('0');
  });

  it('acepta cuerpos de 7 y 8 dígitos', () => {
    expect(digitoVerificadorEsperado('1222232')).not.toBeNull();
    expect(digitoVerificadorEsperado('12222323')).not.toBeNull();
  });

  it('devuelve null si el cuerpo es demasiado corto o largo', () => {
    expect(digitoVerificadorEsperado('123456')).toBeNull();
    expect(digitoVerificadorEsperado('123456789')).toBeNull();
  });
});

describe('validarRut', () => {
  it('acepta RUT válidos de 8 y 7 cifras, con y sin separadores', () => {
    expect(validarRut('12.345.678-5')).toBe(true);
    expect(validarRut('123456785')).toBe(true);
    expect(validarRut('11.111.111-1')).toBe(true);
    expect(validarRut('21.365.487-K')).toBe(true);
    expect(validarRut('21365487K')).toBe(true);
    expect(validarRut('7.600.000-K')).toBe(true);
  });

  it('rechaza dígitos verificadores incorrectos', () => {
    expect(validarRut('11.111.111-9')).toBe(false);
    expect(validarRut('12.345.678-9')).toBe(false);
    expect(validarRut('21.365.487-3')).toBe(false);
  });

  it('rechaza formatos inválidos', () => {
    expect(validarRut('')).toBe(false);
    expect(validarRut('123')).toBe(false);
    expect(validarRut('abc-def')).toBe(false);
  });
});

describe('formatearRut', () => {
  it('formatea un cuerpo de 8 cifras con su DV', () => {
    expect(formatearRut('123456785')).toBe('12.345.678-5');
    expect(formatearRut('12.345.678-5')).toBe('12.345.678-5');
  });

  it('formatea un cuerpo de 7 cifras con su DV sin perderlo', () => {
    // Al tipear 8 dígitos no se sabe si el cuerpo es de 7 u 8. Si la lectura
    // de 7 cifras verifica, es esa: antes el DV se perdía y el RUT quedaba mal.
    expect(formatearRut('12223234')).toBe('1.222.323-4');
    expect(formatearRut('98765433')).toBe('9.876.543-3');
  });

  it('mantiene el cuerpo de 8 cifras mientras falta el DV', () => {
    // "12.345.678" todavía no es un RUT completo: no debe inventar un guion.
    expect(formatearRut('12345678')).toBe('12.345.678');
  });

  it('coloca el guion antes del K', () => {
    expect(formatearRut('21365487k')).toBe('21.365.487-K');
  });

  it('solo agrupa puntos mientras el cuerpo está incompleto', () => {
    expect(formatearRut('12')).toBe('12');
    expect(formatearRut('1234')).toBe('1.234');
  });

  it('nunca altera los dígitos que el usuario escribió', () => {
    for (const rut of [
      '12.345.678-5',
      '1.222.323-4',
      '9.876.543-3',
      '21.365.487-K',
      '7.600.000-K',
    ]) {
      expect(soloDigitos(formatearRut(rut))).toBe(soloDigitos(rut));
    }
  });
});

describe('el RUT sobrevive al tipeo completo', () => {
  it('acepta RUT válidos escritos con puntos, guion o sin separadores', () => {
    const casos = [
      '12.345.678-5', // cuerpo 8
      '123456785', // cuerpo 8, sin formato
      '1.222.323-4', // cuerpo 7
      '12223234', // cuerpo 7, sin formato
      '9.876.543-3', // cuerpo 7
      '7.600.000-K', // cuerpo 7 con K
      '21.365.487-K', // cuerpo 8 con K
    ];

    for (const rut of casos) {
      expect(validarRut(tipear(rut))).toBe(true);
    }
  });
});

describe('rutCanónico', () => {
  it('deja el RUT en la forma en que se guarda', () => {
    expect(rutCanónico('12.345.678-5')).toBe('12.345.678-5');
    expect(rutCanónico('123456785')).toBe('12.345.678-5');
    expect(rutCanónico('21.365.487-K')).toBe('21.365.487-K');
  });

  it('resuelve la ambigüedad de 8 dígitos a favor del cuerpo de 7', () => {
    expect(rutCanónico('12223234')).toBe('1.222.323-4');
    expect(rutCanónico('1.222.323-4')).toBe('1.222.323-4');
  });

  it('devuelve null si el RUT está incompleto o es inválido', () => {
    expect(rutCanónico('12.345.678')).toBeNull(); // falta el DV
    expect(rutCanónico('12.345')).toBeNull();
    expect(rutCanónico('')).toBeNull();
    expect(rutCanónico('21.365.487-3')).toBeNull(); // el DV correcto es K
  });
});

describe('formatearMiles y parsearMiles', () => {
  it('formatea y desformatea montos', () => {
    expect(formatearMiles(530000)).toBe('530.000');
    expect(formatearMiles('590000')).toBe('590.000');
    expect(parsearMiles('530.000')).toBe(530000);
    expect(parsearMiles('1.250.000')).toBe(1250000);
  });

  it('devuelve 0 ante un valor no numérico', () => {
    expect(parsearMiles('')).toBe(0);
    expect(parsearMiles('abc')).toBe(0);
  });
});
