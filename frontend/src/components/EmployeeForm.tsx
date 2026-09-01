// Formulario de alta/edición de empleados (solo administrador).
import { useState, type FormEvent } from 'react'
import type { EmployeeRequest, EmployeeResponse } from '../types'

interface EmployeeFormProps {
  initial?: EmployeeResponse | null
  onSubmit: (data: EmployeeRequest) => Promise<void>
  onCancel: () => void
  submitLabel?: string
}

interface EmployeeFormState extends Omit<EmployeeRequest, 'baseSalary'> {
  baseSalary: string
}

const emptyForm: EmployeeFormState = {
  rut: '',
  firstName: '',
  lastName: '',
  phone: '',
  email: '',
  hireDate: '',
  baseSalary: '0',
  dependents: 0,
  pensionSystem: 'AFP',
  afpFund: '',
  healthSystem: 'FONASA',
}

/**
 * Valida un RUT chileno (formato y dígito verificador por módulo 11, algoritmo del SII).
 * Acepta guion opcional, puntos o sin separador, y DV K/k.
 */
function validarRut(rut: string): boolean {
  const limpio = rut.trim().replace(/[.\-]/g, '')
  if (!/^\d{7,8}[0-9kK]$/.test(limpio)) return false
  const body = limpio.slice(0, -1)
  const dv = limpio.slice(-1).toUpperCase()
  const digito = (() => {
    let suma = 0
    let mult = 2
    for (let i = body.length - 1; i >= 0; i--) {
      suma += Number(body[i]) * mult
      mult = mult === 7 ? 2 : mult + 1
    }
    const resto = suma % 11
    const calc = 11 - resto
    return calc === 11 ? '0' : calc === 10 ? 'K' : String(calc)
  })()
  return dv === digito
}

/**
 * Formatea la entrada de un RUT automáticamente mientras se escribe, con puntos
 * de miles y guion antes del dígito verificador:
 *   12222232K  -> 12.222.232-K
 */
function formatearRut(valor: string): string {
  // Se conservan solo dígitos y la letra K (como DV). El resto se descarta.
  const cuerpo = valor.replace(/[^\d]/g, '')
  const tieneK = /[kK]/.test(valor)

  // Si el usuario escribió K, es el DV; el resto son los dígitos del cuerpo.
  if (tieneK) {
    const dv = 'K'
    const body = cuerpo.slice(0, 8)
    return agregarPuntos(body) + '-' + dv
  }

  // Sin K: el último dígito (una vez alcanzado el largo de RUT completo) es el DV.
  // Se reserva el último dígito como DV cuando se exceden 8 dígitos (7-8 de cuerpo).
  if (cuerpo.length > 8) {
    const body = cuerpo.slice(0, 8)
    const dv = cuerpo.slice(8, 9)
    return agregarPuntos(body) + '-' + dv
  }
  // Aún no se completa el cuerpo: solo se agrupan los dígitos con puntos.
  return agregarPuntos(cuerpo)
}

/** Agrega puntos de miles a la parte numérica del cuerpo del RUT. */
function agregarPuntos(digitos: string): string {
  return digitos.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

/** Formatea un número como string con puntos de miles (ej: 590000 -> "590.000"). */
function formatearSueldo(valor: number | string): string {
  const num = typeof valor === 'string' ? parseInt(valor.replace(/\./g, ''), 10) : valor
  if (isNaN(num)) return ''
  return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

/** Parsea un string formateado con puntos a número (ej: "590.000" -> 590000). */
function parsearSueldo(valor: string): number {
  return parseInt(valor.replace(/\./g, ''), 10) || 0
}

// Convierte un empleado (respuesta) al formulario editable.
function toForm(data: EmployeeResponse | null | undefined): EmployeeFormState {
  if (!data) return { ...emptyForm }
  return {
    rut: formatearRut(data.rut),
    firstName: data.firstName,
    lastName: data.lastName,
    phone: data.phone ?? '',
    email: data.email ?? '',
    hireDate: data.hireDate,
    baseSalary: formatearSueldo(data.baseSalary),
    dependents: data.dependents,
    pensionSystem: data.pensionSystem ?? 'AFP',
    afpFund: data.afpFund ?? '',
    healthSystem: data.healthSystem ?? 'FONASA',
  }
}

export function EmployeeForm({ initial, onSubmit, onCancel, submitLabel }: EmployeeFormProps) {
  const [form, setForm] = useState<EmployeeFormState>(() => toForm(initial))
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  // Para mostrar la validación del RUT solo después de que el usuario lo escriba.
  const [rutTouched, setRutTouched] = useState(false)

  // El RUT debe ser obligatorio y válido (módulo 11).
  const rutValido = form.rut.trim().length > 0 && validarRut(form.rut)
  const mostrarErrorRut = rutTouched && form.rut.trim().length > 0 && !rutValido

  // Actualiza un campo del formulario (con soporte para checkboxes).
  const update = <K extends keyof EmployeeFormState>(key: K, value: EmployeeFormState[K]) => {
    setForm((f) => ({ ...f, [key]: value }))
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    // Bloquear el envío si el RUT es inválido.
    if (!rutValido) {
      setRutTouched(true)
      setError('El RUT no es válido. Verifique el dígito verificador.')
      return
    }
    // Parsear sueldo base formateado a número antes de enviar.
    const payload = {
      ...form,
      baseSalary: parsearSueldo(form.baseSalary)
    }
    setSaving(true)
    try {
      await onSubmit(payload)
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      setError((err as any)?.response?.data?.message ?? 'Error al guardar el empleado')
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-row">
        <div className="form-group">
          <label>RUT</label>
          <input
            className={
              'form-control ' +
              (form.rut.trim().length === 0
                ? ''
                : rutValido
                  ? 'is-valid'
                  : 'is-invalid')
            }
            value={form.rut}
            onChange={(e) => update('rut', formatearRut(e.target.value))}
            onBlur={() => setRutTouched(true)}
            placeholder="12.222.232-K"
            required
          />
          {mostrarErrorRut && <div className="text-danger form-hint">RUT inválido o dígito verificador incorrecto</div>}
          {form.rut.trim().length > 0 && rutValido && (
            <div className="form-hint text-valid">RUT válido</div>
          )}
        </div>
        <div className="form-group">
          <label>Fecha de ingreso</label>
          <input
            type="date"
            className="form-control"
            value={form.hireDate}
            onChange={(e) => update('hireDate', e.target.value)}
            required
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label>Nombre</label>
          <input
            className="form-control"
            value={form.firstName}
            onChange={(e) => update('firstName', e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Apellido</label>
          <input
            className="form-control"
            value={form.lastName}
            onChange={(e) => update('lastName', e.target.value)}
            required
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label>Teléfono</label>
          <input
            className="form-control"
            value={form.phone ?? ''}
            onChange={(e) => update('phone', e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input
            type="email"
            className="form-control"
            value={form.email ?? ''}
            onChange={(e) => update('email', e.target.value)}
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label>Sueldo base (CLP)</label>
          <input
            type="text"
            className="form-control"
            value={form.baseSalary}
            onChange={(e) => update('baseSalary', formatearSueldo(e.target.value))}
            placeholder="Ej: 590.000 o 1.000.000"
            required
          />
          <div className="text-muted form-hint">Formato con puntos de miles (ej: 590.000)</div>
        </div>
      </div>

      <div className="form-row-3">
        <div className="form-group">
          <label>Cargas</label>
          <input
            type="number"
            min="0"
            className="form-control"
            value={form.dependents}
            onChange={(e) => update('dependents', Number(e.target.value))}
          />
          <div className="text-muted form-hint">
            El número de cargas determina la asignación familiar en la liquidación (ley chilena).
          </div>
        </div>
        <div className="form-group">
          <label>Sistema de pensión</label>
          <select
            className="form-control"
            value={form.pensionSystem ?? 'AFP'}
            onChange={(e) => update('pensionSystem', e.target.value)}
          >
            <option value="AFP">AFP</option>
            <option value="IPS">IPS / INP</option>
          </select>
        </div>
        <div className="form-group">
          <label>Fondo AFP</label>
          <input
            className="form-control"
            value={form.afpFund ?? ''}
            onChange={(e) => update('afpFund', e.target.value)}
            placeholder="Ej: HABITAT, CAPITAL"
          />
        </div>
      </div>

      <div className="form-group">
        <label>Sistema de salud</label>
        <select
          className="form-control"
          value={form.healthSystem ?? 'FONASA'}
          onChange={(e) => update('healthSystem', e.target.value)}
        >
          <option value="FONASA">FONASA</option>
          <option value="ISAPRE">ISAPRE</option>
        </select>
      </div>

      {initial && (
        <div className="form-row">
          <div className="form-group">
            <label>Código de marcaje</label>
            <div className="form-control form-control-static">
              <strong>{initial.code}</strong>
            </div>
            <div className="text-muted form-hint">
              El trabajador usa este código para marcar su asistencia (no tiene usuario ni contraseña).
            </div>
          </div>
        </div>
      )}

      {!initial && (
        <div className="alert alert-info">
          Al crear el empleado el sistema le generará automáticamente un código de marcaje
          único; anótelo y entrégueselo al trabajador.
        </div>
      )}

      <div className="flex gap-2 justify-between mt-2">
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          Cancelar
        </button>
        <button type="submit" className="btn" disabled={saving}>
          {saving ? 'Guardando...' : submitLabel ?? 'Guardar'}
        </button>
      </div>
    </form>
  )
}
