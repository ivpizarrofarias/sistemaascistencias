// Página de horarios SEMANALES por empleado (solo administrador).
// Muestra en una lista TODOS los empleados con su horario de la semana
// seleccionada (rotativo). Cada fila permite editar (modal) o eliminar el horario.
import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { employeesApi } from '../api/employees'
import { schedulesApi } from '../api/schedules'
import type { EmployeeResponse, ScheduleDay, WeeklySchedule } from '../types'
import { DIAS_SEMANA, formatDate, formatTime } from '../types'
import { Modal } from '../components/Modal'

// Convierte una fecha (yyyy-MM-dd) de su zona local a su correspondiente lunes.
function mondayOf(dateStr: string): string {
  if (!dateStr) return dateStr
  const [y, m, d] = dateStr.split('-').map(Number)
  const date = new Date(y, m - 1, d)
  const offset = (date.getDay() + 6) % 7
  date.setDate(date.getDate() - offset)
  const yy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yy}-${mm}-${dd}`
}

// Suma días a una fecha yyyy-MM-dd.
function addDaysISO(dateStr: string, days: number): string {
  const [y, m, d] = dateStr.split('-').map(Number)
  const date = new Date(y, m - 1, d)
  date.setDate(date.getDate() + days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// Fecha (yyyy-MM-dd) del lunes de la semana actual.
function currentMonday(): string {
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  return mondayOf(today)
}

// Descripción de un día (trabaja + horas, o "—").
function dayText(sched: WeeklySchedule | undefined, index: number): string {
  const day = sched?.days?.find((d) => d.dayNumber === index + 1)
  if (!day || !day.worked) return '—'
  if (day.startTime && day.endTime) return `${formatTime(day.startTime)}-${formatTime(day.endTime)}`
  return 'Sí'
}

// Plantilla de una semana nueva (6 días lun-sáb).
function emptyWeek(emp: EmployeeResponse, monday: string): WeeklySchedule {
  return {
    id: null,
    employeeId: emp.id,
    employeeName: emp.fullName,
    startDate: monday,
    endDate: addDaysISO(monday, 5),
    label: '',
    days: Array.from({ length: 6 }, (_, i) => ({
      id: null,
      dayNumber: i + 1,
      worked: i < 5,
      startTime: '09:00',
      endTime: '18:00',
    })),
  }
}

export function SchedulesPage() {
  const [employees, setEmployees] = useState<EmployeeResponse[]>([])
  const [weekStart, setWeekStart] = useState<string>(currentMonday())
  const [weeks, setWeeks] = useState<Record<number, WeeklySchedule>>({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  // Editor de la semana de un empleado (modal).
  const [editing, setEditing] = useState<{ emp: EmployeeResponse; sched: WeeklySchedule } | null>(null)
  const [saving, setSaving] = useState(false)

  // Carga el catálogo de empleados al montar.
  useEffect(() => {
    employeesApi
      .findAll()
      .then((emps) => setEmployees(emps.filter((e) => e.enabled)))
      .catch(() => setError('No se pudieron cargar los empleados'))
  }, [])

  const loadWeek = useCallback(async () => {
    if (employees.length === 0) return
    const monday = mondayOf(weekStart)
    setLoading(true)
    setError('')
    try {
      const entries = await Promise.all(
        employees.map(async (e) => {
          try {
            const w = await schedulesApi.getByEmployeeAndWeek(e.id, monday)
            return [e.id, w] as const
          } catch {
            return [e.id, null] as const
          }
        }),
      )
      const map: Record<number, WeeklySchedule> = {}
      for (const [id, w] of entries) if (w) map[id] = w
      setWeeks(map)
    } catch {
      setError('No se pudieron cargar los horarios')
    } finally {
      setLoading(false)
    }
  }, [employees, weekStart])

  // Recarga la lista cuando cambian los empleados o la semana.
  useEffect(() => {
    loadWeek()
  }, [loadWeek])

  const openEditor = (emp: EmployeeResponse) => {
    const existing = weeks[emp.id]
    const sched: WeeklySchedule = existing && existing.days?.length
      ? JSON.parse(JSON.stringify(existing))
      : emptyWeek(emp, mondayOf(weekStart))
    setEditing({ emp, sched })
  }

  const updateDay = (index: number, patch: Partial<ScheduleDay>) => {
    if (!editing) return
    const days = editing.sched.days.map((d, i) => (i === index ? { ...d, ...patch } : d))
    setEditing({ ...editing, sched: { ...editing.sched, days } })
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    if (!editing) return
    setSaving(true)
    setMessage('')
    setError('')
    try {
      const payload = {
        employeeId: editing.emp.id,
        startDate: editing.sched.startDate,
        label: editing.sched.label || 'Semana del ' + formatDate(editing.sched.startDate),
        days: editing.sched.days.map((d) => ({
          dayNumber: d.dayNumber,
          worked: d.worked,
          startTime: d.worked ? d.startTime : null,
          endTime: d.worked ? d.endTime : null,
        })),
      }
      if (editing.sched.id) {
        await schedulesApi.update(editing.sched.id, payload)
      } else {
        await schedulesApi.save(payload)
      }
      setMessage('Horario guardado correctamente')
      setEditing(null)
      await loadWeek()
    } catch {
      setError('No se pudo guardar el horario')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (emp: EmployeeResponse) => {
    const sched = weeks[emp.id]
    if (!sched || !sched.id) return
    if (!window.confirm(`¿Eliminar el horario de ${emp.fullName} para esta semana?`)) return
    await schedulesApi.remove(sched.id)
    await loadWeek()
  }

  // Descarga el PDF del horario de un empleado para la semana seleccionada.
  const handleGenerarPdf = async (emp: EmployeeResponse) => {
    setError('')
    try {
      await schedulesApi.downloadPdf(emp.id, mondayOf(weekStart))
    } catch {
      setError('No se pudo generar el PDF del horario')
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Horarios</h2>
      </div>

      <div className="card mb-2">
        <p className="text-muted">
          Los horarios son <strong>rotativos</strong>: cada semana defina qué días
          (lunes a sábado) trabaja el empleado y a qué horas. No se asocian al
          formulario del trabajador.
        </p>
        <div className="form-row">
          <div className="form-group mb-0">
            <label>Semana (desde el lunes)</label>
            <input
              type="date"
              className="form-control"
              value={weekStart}
              onChange={(e) => setWeekStart(e.target.value)}
            />
          </div>
          <div className="form-group mb-0">
            <label>&nbsp;</label>
            <button className="btn" type="button" onClick={() => setWeekStart(currentMonday())}>
              Semana actual
            </button>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-success">{message}</div>}

      {loading && employees.length === 0 ? (
        <div className="loading">Cargando...</div>
      ) : (
        <div className="card">
          <h4 className="mt-0">
            Semana del {formatDate(mondayOf(weekStart))} al{' '}
            {formatDate(addDaysISO(mondayOf(weekStart), 5))}
          </h4>
          {employees.length === 0 ? (
            <div className="empty">No hay empleados registrados</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Empleado</th>
                  {DIAS_SEMANA.map((dia) => (
                    <th key={dia} className="hide-sm">{dia}</th>
                  ))}
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {employees.map((emp) => (
                  <tr key={emp.id}>
                    <td>
                      {emp.fullName}
                      <div className="text-muted">
                        <span className="badge badge-blue">{emp.code}</span>
                      </div>
                    </td>
                    {DIAS_SEMANA.map((_, i) => (
                      <td key={i} className="hide-sm">{dayText(weeks[emp.id], i)}</td>
                    ))}
                    <td>
                      <div className="flex gap-2">
                        <button className="btn btn-sm btn-secondary" onClick={() => openEditor(emp)}>
                          Editar
                        </button>
                        <button className="btn btn-sm" onClick={() => handleGenerarPdf(emp)}>
                          Generar horario
                        </button>
                        {weeks[emp.id]?.id && (
                          <button className="btn btn-sm btn-danger" onClick={() => handleDelete(emp)}>
                            Eliminar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {loading && employees.length > 0 && (
            <div className="loading">Actualizando...</div>
          )}
        </div>
      )}

      {/* Modal editor de horario de la semana de un empleado */}
      <Modal
        open={editing !== null}
        title={editing ? 'Horario de ' + editing.emp.fullName : ''}
        onClose={() => setEditing(null)}
      >
        {editing && (
          <form onSubmit={handleSave}>
            <p className="text-muted">
              Semana del {formatDate(editing.sched.startDate)} al{' '}
              {formatDate(editing.sched.endDate)}
            </p>
            <div className="form-group">
              <label>Etiqueta (opcional, p. ej. "Semana A")</label>
              <input
                className="form-control"
                value={editing.sched.label ?? ''}
                onChange={(e) => setEditing({ ...editing, sched: { ...editing.sched, label: e.target.value } })}
                placeholder={'Semana del ' + formatDate(editing.sched.startDate)}
              />
            </div>
            <table className="table">
              <thead>
                <tr>
                  <th>Día</th>
                  <th>Trabaja</th>
                  <th>Entrada</th>
                  <th>Salida</th>
                </tr>
              </thead>
              <tbody>
                {editing.sched.days.map((d, i) => (
                  <tr key={d.dayNumber}>
                    <td>{DIAS_SEMANA[i]}</td>
                    <td>
                      <input
                        type="checkbox"
                        checked={d.worked}
                        onChange={(e) => updateDay(i, { worked: e.target.checked })}
                      />
                    </td>
                    <td>
                      <input
                        type="time"
                        className="form-control"
                        value={d.worked ? d.startTime ?? '' : ''}
                        disabled={!d.worked}
                        onChange={(e) => updateDay(i, { startTime: e.target.value || null })}
                      />
                    </td>
                    <td>
                      <input
                        type="time"
                        className="form-control"
                        value={d.worked ? d.endTime ?? '' : ''}
                        disabled={!d.worked}
                        onChange={(e) => updateDay(i, { endTime: e.target.value || null })}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="flex gap-2 justify-between mt-2">
              <button type="button" className="btn btn-secondary" onClick={() => setEditing(null)}>
                Cancelar
              </button>
              <button type="submit" className="btn" disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar horario'}
              </button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  )
}
