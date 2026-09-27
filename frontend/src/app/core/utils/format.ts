/** Helpers de formato compartidos por todas las pantallas. */

/** Formatea un monto en pesos chilenos, sin decimales. */
export function formatMoney(value: number | null | undefined): string {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value)
}

/** Formatea una fecha `yyyy-MM-dd` (o ISO) como dd/mm/aaaa. */
export function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const iso = value.length === 10 ? `${value}T00:00:00` : value
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '-'
  return d.toLocaleDateString('es-CL')
}

/** Recorta una hora `HH:mm:ss` a `HH:mm`. */
export function formatTime(value: string | null | undefined): string {
  if (!value) return '-'
  return value.slice(0, 5)
}

/** Devuelve la fecha de hoy en `yyyy-MM-dd` (hora local, no UTC). */
export function todayIso(): string {
  return toIsoDate(new Date())
}

/** Convierte un `Date` a `yyyy-MM-dd` usando la zona horaria local. */
export function toIsoDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** Suma (o resta) días a una fecha `yyyy-MM-dd` y devuelve otro `yyyy-MM-dd`. */
export function addDaysIso(iso: string, days: number): string {
  const d = new Date(`${iso}T00:00:00`)
  d.setDate(d.getDate() + days)
  return toIsoDate(d)
}

/** Primer día del mes de una fecha `yyyy-MM-dd`. */
export function firstDayOfMonthIso(year: number, month: number): string {
  return `${year}-${String(month).padStart(2, '0')}-01`
}

/** Último día del mes de una fecha `yyyy-MM-dd`. */
export function lastDayOfMonthIso(year: number, month: number): string {
  return toIsoDate(new Date(year, month, 0))
}

/** Etiqueta corta de mes: "Sept 2026". */
export function monthLabel(year: number, month: number): string {
  const d = new Date(year, month - 1, 1)
  const name = d.toLocaleDateString('es-CL', { month: 'short' })
  return `${name.charAt(0).toUpperCase()}${name.slice(1)} ${year}`
}

/**
 * Nombre del PDF de una liquidación:
 * `Liquidación_Javiera_Muñoz_Septiembre_2026.pdf`
 *
 * Es el respaldo que se usa si la respuesta del backend no trae
 * `Content-Disposition`.
 */
export function liquidacionFileName(
  employeeName: string,
  year: number,
  month: number,
): string {
  const nombre = employeeName.trim().replace(/\s+/g, '_')
  const mes = new Date(year, month - 1, 1).toLocaleDateString('es-CL', { month: 'long' })
  const mesTitle = mes.charAt(0).toUpperCase() + mes.slice(1)
  return `Liquidación_${nombre}_${mesTitle}_${year}.pdf`
}

/**
 * Devuelve el lunes de la semana a la que pertenece `iso`.
 * El backend indexa los horarios semanales por su fecha de lunes.
 */
export function mondayOfIso(iso: string): string {
  const d = new Date(`${iso}T00:00:00`)
  const day = d.getDay() // 0 = domingo
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  return toIsoDate(d)
}

/** Número de días entre dos fechas `yyyy-MM-dd`, inclusive. */
export function daysBetweenIso(from: string, to: string): number {
  const a = new Date(`${from}T00:00:00`).getTime()
  const b = new Date(`${to}T00:00:00`).getTime()
  return Math.round((b - a) / 86_400_000) + 1
}

/** Quita acentos y pasa a minúsculas, para búsquedas tolerantes. */
export function normalizeText(value: string | null | undefined): string {
  return (value ?? '')
    .toString()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}
