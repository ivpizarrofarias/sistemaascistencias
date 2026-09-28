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
    // Serie oficial del SII: 2,3,4,5,6,7 y vuelve a 2.
    mult = mult === 7 ? 2 : mult + 1;
  }
  const resto = suma % 11;
  const calc = 11 - resto;
  const digito = calc === 11 ? '0' : calc === 10 ? 'K' : String(calc);

  return dv === digito;
}

/**
 * Calcula el dígito verificador oficial (módulo 11 del SII, pesos 2..7).
 * Devuelve `'0'`, `'K'` o un dígito. Devuelve `null` si el cuerpo no tiene
 * entre 7 y 8 dígitos.
 */
export function digitoVerificadorEsperado(rutSinDv: string): string | null {
  const body = rutSinDv.replace(/\D/g, '');
  if (body.length < 7 || body.length > 8) return null;

  let suma = 0;
  let mult = 2;
  for (let i = body.length - 1; i >= 0; i--) {
    suma += Number(body[i]) * mult;
    // Serie oficial del SII: 2,3,4,5,6,7 y vuelve a 2.
    mult = mult === 7 ? 2 : mult + 1;
  }
  const calc = 11 - (suma % 11);
  return calc === 11 ? '0' : calc === 10 ? 'K' : String(calc);
}

/**
 * Deja el RUT en su forma canónica `12.345.678-5`, resolviendo la ambigüedad de
 * los 8 dígitos: puede ser un cuerpo de 8 cifras con el DV ya escrito, o un
 * cuerpo de 7 con el DV escrito. Se elige la lectura que verifica.
 *
 * Devuelve `null` si no se puede resolver.
 */
export function rutCanónico(rut: string): string | null {
  const tieneK = /[kK]/.test(rut);
  const digitos = rut.replace(/\D/g, '');

  if (tieneK) {
    const dv = digitoVerificadorEsperado(digitos);
    return dv === 'K' ? `${agregarPuntos(digitos)}-K` : null;
  }

  if (digitos.length > 8) {
    const cuerpo = digitos.slice(0, 8);
    const dv = digitos.slice(8, 9);
    if (digitoVerificadorEsperado(cuerpo) !== dv.toUpperCase()) return null;
    return `${agregarPuntos(cuerpo)}-${dv}`;
  }

  if (digitos.length === 8) {
    const cuerpoDe7 = digitos.slice(0, 7);
    if (digitoVerificadorEsperado(cuerpoDe7) === digitos.slice(7, 8)) {
      return `${agregarPuntos(cuerpoDe7)}-${digitos.slice(7, 8)}`;
    }
    // Si la lectura de 7 cifras no verifica, es un cuerpo de 8 que aún no
    // tiene DV: no se puede canonicalizar todavía.
    return null;
  }

  return null;
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
 *
 * Ojo con el caso ambiguo: al tipear 8 dígitos todavía no se sabe si son el
 * cuerpo de un RUT de 8 cifras (y falta el DV) o el cuerpo de 7 cifras con el
 * DV ya escrito. Si la lectura de 7 cifras da un RUT válido, se asume que es
 * esa; si no, se sigue tratando como cuerpo de 8 cifras.
 */
export function formatearRut(valor: string): string {
  // Se conservan solo dígitos y la letra K (como DV). El resto se descarta.
  const cuerpo = valor.replace(/[^\d]/g, '');
  const tieneK = /[kK]/.test(valor);

  // Si el usuario escribió K, esa es el DV y el resto son los dígitos del cuerpo.
  if (tieneK) {
    return `${agregarPuntos(cuerpo.slice(0, 8))}-K`;
  }

  // 9 dígitos o más: el cuerpo es de 8 y el noveno es el DV. No hay ambigüedad.
  if (cuerpo.length > 8) {
    return `${agregarPuntos(cuerpo.slice(0, 8))}-${cuerpo.slice(8, 9)}`;
  }

  // Exactamente 8 dígitos: puede ser cuerpo de 8 (falta el DV) o cuerpo de 7
  // con el DV ya tipeado. Si la lectura de 7 cifra verifica, es un RUT real.
  if (cuerpo.length === 8) {
    const cuerpoDe7 = cuerpo.slice(0, 7);
    if (digitoVerificadorEsperado(cuerpoDe7) === cuerpo.slice(7, 8)) {
      return `${agregarPuntos(cuerpoDe7)}-${cuerpo.slice(7, 8)}`;
    }
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
