/** Utilidades de RUT chileno y formato de montos para los formularios. */

/**
 * Valida un RUT chileno (formato y dígito verificador por módulo 11, algoritmo del SII).
 *
 * Acepta guion opcional, puntos o sin separador, y DV `K` o `k`.
 */
export function validarRut(rut: string): boolean {
  const limpio = rut.trim().replace(/[.\-]/g, '');
  if (!/^\d{7,8}[0-9kK]$/.test(limpio)) return false;

  const body = limpio.slice(0, -1);
  const dv = limpio.slice(-1).toUpperCase();

  let suma = 0;
  let mult = 2;
  for (let i = body.length - 1; i >= 0; i--) {
    suma += Number(body[i]) * mult;
    mult = mult === 7 ? 2 : mult + 1;
  }
  const resto = suma % 11;
  const calc = 11 - resto;
  const digito = calc === 11 ? '0' : calc === 10 ? 'K' : String(calc);

  return dv === digito;
}

/** Agrega puntos de miles a la parte numérica del cuerpo del RUT. */
function agregarPuntos(digitos: string): string {
  return digitos.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

/**
 * Formatea la entrada de un RUT mientras se escribe, con puntos de miles y
 * guion antes del dígito verificador.
 *
 * `12222232K` -> `12.222.232-K`
 */
export function formatearRut(valor: string): string {
  // Se conservan solo dígitos y la letra K (como DV). El resto se descarta.
  const cuerpo = valor.replace(/[^\d]/g, '');
  const tieneK = /[kK]/.test(valor);

  // Si el usuario escribió K, esa es el DV y el resto son los dígitos del cuerpo.
  if (tieneK) {
    return `${agregarPuntos(cuerpo.slice(0, 8))}-K`;
  }

  // Sin K: al superar los 8 dígitos, el noveno es el DV.
  if (cuerpo.length > 8) {
    return `${agregarPuntos(cuerpo.slice(0, 8))}-${cuerpo.slice(8, 9)}`;
  }

  // Cuerpo incompleto: solo se agrupan los dígitos con puntos.
  return agregarPuntos(cuerpo);
}

/** Formatea un número como texto con puntos de miles: `590000` -> `590.000`. */
export function formatearMiles(valor: number | string): string {
  const num = typeof valor === 'string' ? Number.parseInt(valor.replace(/\./g, ''), 10) : valor;
  if (Number.isNaN(num)) return '';
  return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

/** Convierte un texto con puntos de miles a número: `590.000` -> `590000`. */
export function parsearMiles(valor: string): number {
  return Number.parseInt(valor.replace(/\./g, ''), 10) || 0;
}
