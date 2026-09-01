// Página de asistencia (administrador).
// Permite ver las asistencias de un empleado, completar/corregir un día,
// editar los tiempos de marcaje y marcar ausencias no justificadas.
import { useCallback, useEffect, useState } from 'react'
import { attendanceApi } from '../api/attendance'
import { employeesApi } from '../api/employees'
import { Modal } from '../components/Modal'
import type { AttendanceResponse, EmployeeOption } from '../types'
import { formatDate, formatTime } from '../types'

function toOptions(emps: EmployeeOption[]): EmployeeOption[] {
  return emps
}

export function AttendancePage() {
  const [employees, setEmployees] = useState<EmployeeOption[]>([])
  const [selectedId, setSelectedId] = useState<number | ''>('')
  const [attendances, setAttendances] = useState<AttendanceResponse[]>([])
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Modal de edición: los tiempos se guardan como strings "HH:mm".
  const [editTarget, setEditTarget] = useState<AttendanceResponse | null>(null)
  const [editTimes, setEditTimes] = useState({
    entryTime: '',
    colacionSalida1: '',
    colacionEntrada1: '',
    exitTime: '',
  })
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  useEffect(() => {
    employeesApi.findAll().then((data) => setEmployees(toOptions(data)))
  }, [])

  const load = useCallback(
    async (employeeId: number) => {
      setLoading(true)
      try {
        const data = await attendanceApi.listByEmployee(employeeId, from || undefined, to || undefined)
        setAttendances(data)
        setError('')
      } catch {
        setError('No se pudieron cargar las asistencias')
      } finally {
        setLoading(false)
      }
    },
    [from, to],
  )

  const handleSelect = (value: number | '') => {
    setSelectedId(value)
    if (value !== '') load(value)
  }

  const handleComplete = async (a: AttendanceResponse) => {
    if (a.complete) {
      // Si ya está completa, la dejamos pendiente para que pueda marcar.
      await attendanceApi.update(a.id, { complete: false })
    } else {
      await attendanceApi.update(a.id, { complete: true })
    }
    if (selectedId !== '') load(selectedId)
  }

  const handleAbsence = async (a: AttendanceResponse, absence: boolean) => {
    if (!window.confirm(`¿Marcar ${a.employeeName} como ${absence ? 'ausente' : 'presente'} el ${formatDate(a.date)}?`))
      return
    await attendanceApi.markAbsence(a.employeeId, a.date, absence)
    if (selectedId !== '') load(selectedId)
  }

  const handleOpenEdit = (a: AttendanceResponse) => {
    setEditTarget(a)
    setEditTimes({
      entryTime: (a.entryTime ?? '').slice(0, 5),
      colacionSalida1: (a.colacionSalida1 ?? '').slice(0, 5),
      colacionEntrada1: (a.colacionEntrada1 ?? '').slice(0, 5),
      exitTime: (a.exitTime ?? '').slice(0, 5),
    })
    setSaveError('')
  }

  const handleSaveEdit = async () => {
    if (!editTarget) return
    setSaving(true)
    setSaveError('')
    try {
      await attendanceApi.update(editTarget.id, {
        entryTime: editTimes.entryTime || null,
        colacionSalida1: editTimes.colacionSalida1 || null,
        colacionEntrada1: editTimes.colacionEntrada1 || null,
        exitTime: editTimes.exitTime || null,
      })
      setEditTarget(null)
      if (selectedId !== '') load(selectedId)
    } catch {
      setSaveError('No se pudo guardar la asistencia')
    } finally {
      setSaving(false)
    }
  }

  const renderTime = (v: string | null) => formatTime(v)

  return (
    <div>
      <div className="page-header">
        <h2>Control de asistencia</h2>
      </div>

      <div className="card">
        <div className="form-row-3">
          <div className="form-group">
            <label>Empleado</label>
            <select
              className="form-control"
              value={selectedId}
              onChange={(e) => handleSelect(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">Seleccione un empleado</option>
              {employees.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.fullName} ({e.rut})
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Desde</label>
            <input
              type="date"
              className="form-control"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Hasta</label>
            <input
              type="date"
              className="form-control"
              value={to}
              onChange={(e) => setTo(e.target.value)}
            />
          </div>
        </div>
        <button
          className="btn btn-secondary"
          onClick={() => selectedId !== '' && load(selectedId)}
          disabled={selectedId === ''}
        >
          Consultar
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading">Cargando...</div>
      ) : (
        selectedId !== '' && (
          <div className="card">
            {attendances.length === 0 ? (
              <div className="empty">Sin registros de asistencia en el período</div>
            ) : (
              <table className="table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Entrada</th>
                    <th className="hide-sm">Col. Salida</th>
                    <th className="hide-sm">Col. Entrada</th>
                    <th>Salida</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {attendances.map((a) => (
                    <tr key={a.id}>
                      <td>{formatDate(a.date)}</td>
                      <td>{renderTime(a.entryTime)}</td>
                      <td className="hide-sm">{renderTime(a.colacionSalida1)}</td>
                      <td className="hide-sm">{renderTime(a.colacionEntrada1)}</td>
                      <td>{renderTime(a.exitTime)}</td>
                      <td>
                        {a.absence && <span className="badge badge-red">Ausente</span>}
                        {a.medicalLicense && <span className="badge badge-blue">Licencia</span>}
                        {!a.absence && !a.medicalLicense && (
                          <span className={'badge ' + (a.complete ? 'badge-green' : 'badge-yellow')}>
                            {a.complete ? 'Completo' : 'Incompleto'}
                          </span>
                        )}
                      </td>
                      <td>
                        <div className="flex gap-2">
                          <button className="btn btn-sm btn-secondary" onClick={() => handleOpenEdit(a)}>
                            Editar
                          </button>
                          <button className="btn btn-sm btn-secondary" onClick={() => handleComplete(a)}>
                            {a.complete ? 'Descompletar' : 'Completar'}
                          </button>
                          {!a.absence && !a.medicalLicense && (
                            <button className="btn btn-sm btn-danger" onClick={() => handleAbsence(a, true)}>
                              Marcar ausente
                            </button>
                          )}
                          {a.absence && (
                            <button className="btn btn-sm btn-success" onClick={() => handleAbsence(a, false)}>
                              Revertir ausencia
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )
      )}

      {/* Modal de edición de tiempos de una asistencia */}
      <Modal
        open={editTarget !== null}
        title={editTarget ? `Editar asistencia ${formatDate(editTarget.date)}` : 'Editar asistencia'}
        onClose={() => setEditTarget(null)}
      >
        {editTarget && (
          <div>
            <p className="mb-2">
              <strong>{editTarget.employeeName}</strong> ({editTarget.rut})
            </p>
            {saveError && <div className="alert alert-error">{saveError}</div>}
            <div className="form-row">
              <div className="form-group">
                <label>Entrada</label>
                <input
                  type="time"
                  className="form-control"
                  value={editTimes.entryTime}
                  onChange={(e) => setEditTimes({ ...editTimes, entryTime: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Salida</label>
                <input
                  type="time"
                  className="form-control"
                  value={editTimes.exitTime}
                  onChange={(e) => setEditTimes({ ...editTimes, exitTime: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Colación salida</label>
                <input
                  type="time"
                  className="form-control"
                  value={editTimes.colacionSalida1}
                  onChange={(e) => setEditTimes({ ...editTimes, colacionSalida1: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Colación entrada</label>
                <input
                  type="time"
                  className="form-control"
                  value={editTimes.colacionEntrada1}
                  onChange={(e) => setEditTimes({ ...editTimes, colacionEntrada1: e.target.value })}
                />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setEditTarget(null)} disabled={saving}>
                Cancelar
              </button>
              <button type="button" className="btn btn-primary" onClick={handleSaveEdit} disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
