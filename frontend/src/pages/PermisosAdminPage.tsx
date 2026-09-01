// Página de permisos (administrador): lista todas las solicitudes, permite
// aprobar/rechazar/eliminar y crear un "Nuevo permiso" a nombre de un empleado.
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { employeesApi } from '../api/employees'
import { permisosApi } from '../api/permisos'
import type {
  EmployeeResponse,
  FormaCompensacion,
  PermisoEstado,
  PermisoLaboralResponse,
  PermisoTipo,
} from '../types'
import {
  FormaCompensacionLabel,
  PermisoEstadoLabel,
  PermisoTipoLabel,
  formatDate,
} from '../types'
import { Modal } from '../components/Modal'

// Suma días a una fecha ISO (yyyy-MM-dd).
function addDaysISO(iso: string, days: number): string {
  const [y, m, d] = iso.split('-').map(Number)
  const date = new Date(y, m - 1, d)
  date.setDate(date.getDate() + days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// Lista las fechas (ISO) de permiso desde la inicial según la cantidad de días.
function fechasPermiso(inicio: string, dias: number): string[] {
  if (!inicio || dias < 1) return []
  return Array.from({ length: dias }, (_, i) => addDaysISO(inicio, i))
}

export function PermisosAdminPage() {
  const [permisos, setPermisos] = useState<PermisoLaboralResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Catálogo de empleados para el formulario "Nuevo permiso".
  const [employees, setEmployees] = useState<EmployeeResponse[]>([])

  // Modal "Nuevo permiso".
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [pEmployeeId, setPEmployeeId] = useState<number | ''>('')
  const [pDate, setPDate] = useState('')
  const [pTipo, setPTipo] = useState<PermisoTipo>('POR_HORA')
  const [pHoras, setPHoras] = useState('')
  const [pDias, setPDias] = useState('')
  const [pMotivo, setPMotivo] = useState('')
  const [pComp, setPComp] = useState<FormaCompensacion>('DESCUENTO')

  // Modal "Registrar devolución" (confirmar que el trabajador ya devolvió el día/horas).
  const [devTarget, setDevTarget] = useState<PermisoLaboralResponse | null>(null)
  const [dHoras, setDHoras] = useState('')
  const [dDias, setDDias] = useState('')
  const [devSaving, setDevSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setPermisos(await permisosApi.findAll())
      setError('')
    } catch {
      setError('No se pudieron cargar los permisos')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const handleValidate = async (p: PermisoLaboralResponse, estado: PermisoEstado) => {
    if (!window.confirm(`¿Marcar el permiso de ${p.employeeName} como ${PermisoEstadoLabel[estado]}?`)) return
    await permisosApi.validate(p.id, { estado })
    await load()
  }

  const handleDelete = async (p: PermisoLaboralResponse) => {
    if (!window.confirm(`¿Eliminar el permiso #${p.id}?`)) return
    await permisosApi.remove(p.id)
    await load()
  }

  // Abre el modal para registrar la devolución (confirmando tiempo devuelto).
  const openDevModal = (p: PermisoLaboralResponse) => {
    setDevTarget(p)
    setDHoras('')
    setDDias('')
    setError('')
  }

  // Confirma la devolución: si el trabajador devolvió el tiempo, no se descuenta en la liquidación.
  const handleDevolucion = async (e: FormEvent) => {
    e.preventDefault()
    if (!devTarget) return
    setDevSaving(true)
    setError('')
    try {
      const payload =
        devTarget.tipo === 'POR_HORA'
          ? { horasDevueltas: Number(dHoras) }
          : { diasDevueltos: Number(dDias) }
      await permisosApi.registerDevolucion(devTarget.id, payload)
      setDevTarget(null)
      await load()
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      setError((err as any)?.response?.data?.message ?? 'No se pudo registrar la devolución')
    } finally {
      setDevSaving(false)
    }
  }

  // Carga el catálogo de empleados para el formulario "Nuevo permiso".
  useEffect(() => {
    employeesApi
      .findAll()
      .then(setEmployees)
      .catch(() => setError('No se pudieron cargar los empleados'))
  }, [])

  const openCreateModal = () => {
    setPEmployeeId('')
    setPDate('')
    setPTipo('POR_HORA')
    setPHoras('')
    setPDias('')
    setPMotivo('')
    setPComp('DESCUENTO')
    setModalOpen(true)
  }

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault()
    if (pEmployeeId === '') {
      setError('Debe seleccionar un empleado')
      return
    }
    setSaving(true)
    setError('')
    try {
      await permisosApi.createAsAdmin(pEmployeeId, {
        date: pDate,
        tipo: pTipo,
        horas: pTipo === 'POR_HORA' ? Number(pHoras) : null,
        dias: pTipo === 'POR_DIA' ? Number(pDias) : null,
        motivo: pMotivo,
        formaCompensacion: pComp,
      })
      setModalOpen(false)
      await load()
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      setError((err as any)?.response?.data?.message ?? 'No se pudo crear el permiso')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Permisos laborales</h2>
        <button className="btn" onClick={openCreateModal}>
          + Nuevo permiso
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {modalOpen && (
        <Modal open title="Nuevo permiso" onClose={() => setModalOpen(false)}>
          <form onSubmit={handleCreate}>
            <div className="form-group">
              <label>Empleado</label>
              <select
                className="form-control"
                value={pEmployeeId}
                onChange={(e) => setPEmployeeId(e.target.value === '' ? '' : Number(e.target.value))}
                required
              >
                <option value="">Seleccione un empleado</option>
                {employees.map((e) => (
                  <option key={e.id} value={e.id}>
                    {e.fullName} ({e.rut})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Fecha</label>
                <input
                  type="date"
                  className="form-control"
                  value={pDate}
                  onChange={(e) => setPDate(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>Tipo</label>
                <select
                  className="form-control"
                  value={pTipo}
                  onChange={(e) => setPTipo(e.target.value as PermisoTipo)}
                >
                  <option value="POR_HORA">Por horas</option>
                  <option value="POR_DIA">Por día</option>
                </select>
              </div>
            </div>
            {pTipo === 'POR_HORA' ? (
              <div className="form-group">
                <label>Horas de ausencia</label>
                <input
                  type="number"
                  min="0.01"
                  step="any"
                  className="form-control"
                  value={pHoras}
                  onChange={(e) => setPHoras(e.target.value)}
                  required
                />
                <div className="form-hint">Cantidad de horas que el empleado estará ausente.</div>
              </div>
            ) : (
              <>
                <div className="form-group">
                  <label>Días de permiso</label>
                  <input
                    type="number"
                    min="1"
                    step="1"
                    className="form-control"
                    value={pDias}
                    onChange={(e) => setPDias(e.target.value)}
                    required
                  />
                </div>
                {pDias && Number(pDias) >= 1 ? (
                  <div className="form-group">
                    <label>Fechas que estará con permiso</label>
                    <ul className="mb-0" style={{ marginTop: 0, paddingLeft: '1.2em' }}>
                      {fechasPermiso(pDate, Number(pDias)).map((f) => (
                        <li key={f}>{formatDate(f)}</li>
                      ))}
                    </ul>
                  </div>
                ) : (
                  <div className="form-hint">
                    Indique la cantidad de días para ver las fechas concretas del permiso.
                  </div>
                )}
              </>
            )}
            <div className="form-group">
              <label>Motivo</label>
              <input
                className="form-control"
                value={pMotivo}
                onChange={(e) => setPMotivo(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Compensación</label>
              <select
                className="form-control"
                value={pComp}
                onChange={(e) => setPComp(e.target.value as FormaCompensacion)}
              >
                <option value="DESCUENTO">Descontar del sueldo</option>
                <option value="DEVOLVER">Devolver después</option>
              </select>
            </div>
            {pComp === 'DEVOLVER' && (
              <div className="form-hint">
                Si el empleado no devuelve el tiempo (parcial o totalmente), el saldo
                pendiente se descuenta en la liquidación.
              </div>
            )}
            <div className="flex gap-2 justify-between mt-2">
              <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>
                Cancelar
              </button>
              <button type="submit" className="btn" disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar permiso'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Modal "Registrar devolución": el admin confirma que el trabajador ya devolvió el tiempo
          (horas o días) para que ese saldo NO se descuente en la liquidación. */}
      <Modal
        open={devTarget !== null}
        title="Registrar devolución del permiso"
        onClose={() => setDevTarget(null)}
      >
        {devTarget && (
          <form onSubmit={handleDevolucion}>
            <p className="text-muted">
              Permiso de <strong>{devTarget.employeeName}</strong> el {formatDate(devTarget.date)} ({PermisoTipoLabel[devTarget.tipo]}
              {devTarget.tipo === 'POR_HORA' ? `, ${devTarget.horas} h` : `, ${devTarget.dias} día/s`}).
              Marque el tiempo que el trabajador <strong>ya devolvió</strong>; ese saldo no se descuenta.
            </p>
            {devTarget.tipo === 'POR_HORA' ? (
              <>
                <div className="form-group">
                  <label>Horas que devuelve ahora</label>
                  <input
                    type="number"
                    min="0.01"
                    max={Math.max((devTarget.horas ?? 0) - (devTarget.horasDevueltas ?? 0), 0)}
                    step="any"
                    className="form-control"
                    value={dHoras}
                    onChange={(e) => setDHoras(e.target.value)}
                    required
                  />
                  <div className="form-hint">
                    Solicitadas: {devTarget.horas} h · Devueltas antes:{' '}
                    {devTarget.horasDevueltas ?? 0} h · Pendiente:{' '}
                    {Math.max((devTarget.horas ?? 0) - (devTarget.horasDevueltas ?? 0), 0)} h
                  </div>
                </div>
              </>
            ) : (
              <div className="form-group">
                <label>Días que devuelve ahora</label>
                <input
                  type="number"
                  min="1"
                  max={Math.max(devTarget.dias - devTarget.diasDevueltos, 0)}
                  step="1"
                  className="form-control"
                  value={dDias}
                  onChange={(e) => setDDias(e.target.value)}
                  required
                />
                <div className="form-hint">
                  Solicitados: {devTarget.dias} día/s · Devueltos antes:{' '}
                  {devTarget.diasDevueltos} · Pendiente:{' '}
                  {Math.max(devTarget.dias - devTarget.diasDevueltos, 0)} día/s
                </div>
              </div>
            )}
            <div className="flex gap-2 justify-between mt-2">
              <button type="button" className="btn btn-secondary" onClick={() => setDevTarget(null)}>
                Cancelar
              </button>
              <button type="submit" className="btn" disabled={devSaving}>
                {devSaving ? 'Guardando...' : 'Confirmar devolución'}
              </button>
            </div>
          </form>
        )}
      </Modal>

      {loading ? (
        <div className="loading">Cargando...</div>
      ) : (
        <div className="card">
          {permisos.length === 0 ? (
            <div className="empty">No hay permisos solicitados</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Fecha</th>
                  <th>Tipo</th>
                  <th className="hide-sm">Motivo</th>
                  <th className="hide-sm">Compensación</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {permisos.map((p) => (
                  <tr key={p.id}>
                    <td>{p.employeeName}</td>
                    <td>{formatDate(p.date)}</td>
                    <td>
                      {PermisoTipoLabel[p.tipo]}
                      {p.tipo === 'POR_HORA' ? ` (${p.horas} h)` : ` (${p.dias} día/s)`}
                    </td>
                    <td className="hide-sm">{p.motivo}</td>
                    <td className="hide-sm">
                      {FormaCompensacionLabel[p.formaCompensacion]}
                      {p.formaCompensacion === 'DEVOLVER' && p.estado === 'APROBADO' && (
                        <div className="text-muted">
                          {p.tipo === 'POR_HORA'
                            ? `Devueltas: ${p.horasDevueltas ?? 0} h / ${p.horas} h`
                            : `Devueltos: ${p.diasDevueltos} / ${p.dias} día/s`}
                          {p.saldoPendiente && ' · pendiente'}
                        </div>
                      )}
                    </td>
                    <td>
                      <span
                        className={
                          'badge ' +
                          (p.estado === 'APROBADO'
                            ? 'badge-green'
                            : p.estado === 'RECHAZADO'
                              ? 'badge-red'
                              : 'badge-yellow')
                        }
                      >
                        {PermisoEstadoLabel[p.estado]}
                      </span>
                    </td>
                    <td>
                      <div className="flex gap-2">
                        {p.estado === 'PENDIENTE' && (
                          <>
                            <button className="btn btn-sm btn-success" onClick={() => handleValidate(p, 'APROBADO')}>
                              Aprobar
                            </button>
                            <button className="btn btn-sm btn-danger" onClick={() => handleValidate(p, 'RECHAZADO')}>
                              Rechazar
                            </button>
                          </>
                        )}
                        {p.estado === 'APROBADO' && p.formaCompensacion === 'DEVOLVER' && p.saldoPendiente && (
                          <button className="btn btn-sm" onClick={() => openDevModal(p)}>
                            Editar
                          </button>
                        )}
                        <button className="btn btn-sm btn-secondary" onClick={() => handleDelete(p)}>
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}
