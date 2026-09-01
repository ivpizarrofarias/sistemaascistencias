// Página de remuneraciones (administrador).
// Muestra una tabla con TODOS los empleados y su última liquidación (columna
// Empleado incluida), permite generar liquidaciones por período (todas o de un
// empleado) y gestiona los anticipos y licencias médicas de cada trabajador.
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { payrollApi } from '../api/payroll'
import { employeesApi } from '../api/employees'
import type { EmployeeResponse, LiquidacionResponse, AdvanceResponse, LicenciaResponse } from '../types'
import { formatDate, formatMoney } from '../types'
import { Modal } from '../components/Modal'

type Tab = 'liquidaciones' | 'anticipos' | 'licencias'

export function PayrollAdminPage() {
  const [employees, setEmployees] = useState<EmployeeResponse[]>([])
  const [liquidacionesMap, setLiquidacionesMap] = useState<Record<number, LiquidacionResponse[]>>({})
  const [selectedId, _setSelectedId] = useState<number | ''>('')

  // Período de visualización de la pestaña Liquidaciones (por defecto, mes actual).
  const [visYear, setVisYear] = useState(String(new Date().getFullYear()))
  const [visMonth, setVisMonth] = useState(String(new Date().getMonth() + 1))

  const [tab, setTab] = useState<Tab>('liquidaciones')
  const [error, setError] = useState('')
  const [_info, _setInfo] = useState('')

  const [viewLiquidacion, setViewLiquidacion] = useState<LiquidacionResponse | null>(null)
  const [viewFromGenerate, setViewFromGenerate] = useState(false)
  const [genYear, setGenYear] = useState(String(new Date().getFullYear()))
  const [genMonth, setGenMonth] = useState(String(new Date().getMonth() + 1))
  const [extras, setExtras] = useState('')

  // Modal para generar liquidaciones de todos
  const [generateAllModal, setGenerateAllModal] = useState(false)
  const [generateAllResult, setGenerateAllResult] = useState<{ count: number; success: boolean } | null>(null)

  // Modal para horas extra del trabajador
  const [horaExtraModal, setHoraExtraModal] = useState<{ open: boolean; employee: EmployeeResponse | null; date: string; horas: string; valor: string }>({
    open: false,
    employee: null,
    date: '',
    horas: '',
    valor: ''
  })

  const [advances, setAdvances] = useState<AdvanceResponse[]>([])
  const [advModal, setAdvModal] = useState(false)
  const [advEmployee, setAdvEmployee] = useState<EmployeeResponse | null>(null)
  const [advDate, setAdvDate] = useState('')
  const [advAmount, setAdvAmount] = useState('')
  const [advDesc, setAdvDesc] = useState('')

  const [licencias, setLicencias] = useState<LicenciaResponse[]>([])
  const [licModal, setLicModal] = useState(false)
  const [licEmployee, setLicEmployee] = useState<EmployeeResponse | null>(null)
  const [licStart, setLicStart] = useState('')
  const [licEnd, setLicEnd] = useState('')
  const [licMotivo, setLicMotivo] = useState('')
  const [licPays, setLicPays] = useState(true)

  // Buscadores para cada pestaña
  const [liqSearch, setLiqSearch] = useState('')
  const [advSearch, setAdvSearch] = useState('')
  const [licSearch, setLicSearch] = useState('')

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const errorMessage = (err: unknown) =>
    (err as any)?.response?.data?.message ?? 'Ocurrió un error inesperado'

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const emps = await employeesApi.findAll()
      setEmployees(emps)
      const map: Record<number, LiquidacionResponse[]> = {}
      const allAdvances: AdvanceResponse[] = []
      const allLicencias: LicenciaResponse[] = []
      await Promise.all(
        emps.map(async (e) => {
          try {
            map[e.id] = await payrollApi.listLiquidaciones(e.id)
          } catch {
            map[e.id] = []
          }
          try {
            allAdvances.push(...(await payrollApi.listAdvances(e.id)))
          } catch {
            /* sin anticipos */
          }
          try {
            allLicencias.push(...(await payrollApi.listLicencias(e.id)))
          } catch {
            /* sin licencias */
          }
        }),
      )
      setLiquidacionesMap(map)
      setAdvances(allAdvances)
      setLicencias(allLicencias)
    } catch {
      setError('No se pudieron cargar los datos de remuneraciones')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadAll()
  }, [loadAll])

  const loadSelected = useCallback(async (id: number) => {
    setError('')
    try {
      setAdvances(await payrollApi.listAdvances(id))
      setLicencias(await payrollApi.listLicencias(id))
    } catch {
      setError('No se pudieron cargar los datos del empleado')
    }
  }, [])

  useEffect(() => {
    if (selectedId !== '') loadSelected(selectedId)
  }, [selectedId, loadSelected])

  const handleGenerateAll = async () => {
    setError('')
    setSaving(true)
    try {
      const res = await payrollApi.liquidarTodos(Number(genYear), Number(genMonth))
      const count = (res as any).count ?? 0
      setGenerateAllResult({ count, success: true })
      setGenerateAllModal(true)
      setTab('liquidaciones')
      await loadAll()
    } catch (err: unknown) {
      setGenerateAllResult({ count: 0, success: false })
      setGenerateAllModal(true)
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleGenerateOne = async (empId: number) => {
    setError('')
    setSaving(true)
    try {
      const liq = await payrollApi.createLiquidacion(empId, Number(genYear), Number(genMonth), extras ? Number(extras) : undefined)
      setViewFromGenerate(true)
      setViewLiquidacion(liq)
      setExtras('')
      await loadAll()
    } catch (err: unknown) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleVerLiquidacion = (liq: LiquidacionResponse) => {
    setViewFromGenerate(false)
    setViewLiquidacion(liq)
  }

  const handlePagar = async (liq: LiquidacionResponse) => {
    if (!window.confirm(`¿Marcar como pagada la liquidación de ${liq.employeeName}?`)) return
    await payrollApi.marcarPagada(liq.id)
    await loadAll()
  }

  const handleAddAdvance = async (e: FormEvent) => {
    e.preventDefault()
    if (!advEmployee) return
    setSaving(true)
    try {
      await payrollApi.addAdvance(advEmployee.id, { date: advDate, amount: Number(advAmount), description: advDesc || null })
      setAdvModal(false)
      setAdvEmployee(null)
      setAdvDate('')
      setAdvAmount('')
      setAdvDesc('')
      await loadAll()
    } catch (err: unknown) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteAdvance = async (a: AdvanceResponse) => {
    if (!window.confirm(`¿Eliminar el anticipo de ${formatMoney(a.amount)}?`)) return
    await payrollApi.deleteAdvance(a.id)
    await loadAll()
  }

  const handleAddLicencia = async (e: FormEvent) => {
    e.preventDefault()
    if (!licEmployee) return
    setSaving(true)
    try {
      await payrollApi.addLicencia(licEmployee.id, {
        startDate: licStart,
        endDate: licEnd,
        motivo: licMotivo,
        employerPays: licPays,
      })
      setLicModal(false)
      setLicEmployee(null)
      setLicStart('')
      setLicEnd('')
      setLicMotivo('')
      await loadAll()
    } catch (err: unknown) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteLicencia = async (l: LicenciaResponse) => {
    if (!window.confirm('¿Eliminar esta licencia médica?')) return
    await payrollApi.deleteLicencia(l.id)
    await loadAll()
  }

  const openHoraExtraModal = (e: EmployeeResponse) => {
    const hoy = new Date().toISOString().split('T')[0]
    setHoraExtraModal({
      open: true,
      employee: e,
      date: hoy,
      horas: '',
      valor: ''
    })
  }

  const handleSaveHoraExtra = async () => {
    if (!horaExtraModal.employee || !horaExtraModal.date || !horaExtraModal.horas || !horaExtraModal.valor) {
      setError('Complete todos los campos')
      return
    }
    setError('')
    setSaving(true)
    try {
      await payrollApi.addHoraExtra(horaExtraModal.employee.id, {
        date: horaExtraModal.date,
        horas: Number(horaExtraModal.horas)
      })
      setHoraExtraModal({ open: false, employee: null, date: '', horas: '', valor: '' })
      await loadAll()
    } catch (err: unknown) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleDescargarPdf = async (liq: LiquidacionResponse) => {
    setError('')
    try {
      await payrollApi.downloadLiquidacionPdf(liq.id)
    } catch (err: unknown) {
      setError(errorMessage(err))
    }
  }

  const nombreMes = (m: number) =>
    ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'][m - 1] ?? String(m)

  const periodo = (l: LiquidacionResponse | null | undefined) =>
    l ? `${String(l.periodMonth).padStart(2, '0')}/${l.periodYear}` : '-'

  // Liquidación de un empleado para el período de visualización seleccionado.
  const liqVis = (empId: number): LiquidacionResponse | null =>
    (liquidacionesMap[empId] ?? []).find(
      (l) => l.periodMonth === Number(visMonth) && l.periodYear === Number(visYear),
    ) ?? null

  return (
    <div>
      <div className="page-header">
        <h2>Remuneraciones</h2>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {_info && <div className="alert alert-success">{_info}</div>}

      {/* Generación por período */}
      <div className="card">
        <div className="flex gap-2 align-end flex-wrap">
          <div className="form-group mb-0">
            <label>Año</label>
            <input type="number" className="form-control" value={genYear} onChange={(e) => setGenYear(e.target.value)} />
          </div>
          <div className="form-group mb-0">
            <label>Mes</label>
            <input type="number" min="1" max="12" className="form-control" value={genMonth} onChange={(e) => setGenMonth(e.target.value)} />
          </div>
          <div className="form-group mb-0">
            <label>&nbsp;</label>
            <button className="btn" onClick={handleGenerateAll} disabled={saving || employees.length === 0}>
              {saving ? 'Generando...' : 'Generar liquidaciones de todos'}
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="flex gap-2">
          <button className={'btn btn-sm ' + (tab === 'liquidaciones' ? '' : 'btn-secondary')} onClick={() => setTab('liquidaciones')}>
            Liquidaciones
          </button>
          <button className={'btn btn-sm ' + (tab === 'anticipos' ? '' : 'btn-secondary')} onClick={() => setTab('anticipos')}>
            Anticipos
          </button>
          <button className={'btn btn-sm ' + (tab === 'licencias' ? '' : 'btn-secondary')} onClick={() => setTab('licencias')}>
            Licencias
          </button>
        </div>

        {tab === 'liquidaciones' && (
          <>
            <div className="form-group mt-2 mb-0">
              <input
                type="text"
                className="form-control"
                placeholder="Buscar por nombre o RUT..."
                value={liqSearch}
                onChange={(e) => setLiqSearch(e.target.value)}
              />
            </div>
            <div className="flex gap-2 mt-2">
              <select className="form-control" style={{ maxWidth: 120 }} value={visMonth} onChange={(e) => setVisMonth(e.target.value)}>
                {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                  <option key={m} value={m}>
                    {nombreMes(m)}
                  </option>
                ))}
              </select>
              <select className="form-control" style={{ maxWidth: 120 }} value={visYear} onChange={(e) => setVisYear(e.target.value)}>
                {Array.from({ length: 6 }, (_, i) => Number(visYear) - 2 + i).map((y) => (
                  <option key={y} value={y}>
                    {y}
                  </option>
                ))}
              </select>
            </div>
            {loading ? (
              <div className="loading">Cargando...</div>
            ) : employees.length === 0 ? (
              <div className="empty">No hay empleados registrados</div>
            ) : (
              <table className="table mt-2">
                <thead>
                  <tr>
                    <th>Empleado</th>
                    <th className="hide-sm">RUT</th>
                    <th>Período</th>
                    <th className="hide-sm">Sueldo base</th>
                    <th>Líquido a pagar</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {employees
                    .filter((e) => e.enabled)
                    .filter((e) => {
                      const q = liqSearch.trim().toLowerCase()
                      if (!q) return true
                      return (
                        e.fullName.toLowerCase().includes(q) ||
                        e.rut.toLowerCase().includes(q)
                      )
                    })
                    .map((e) => {
                      const liq = liqVis(e.id)
                      return (
                        <tr key={e.id}>
                          <td>
                            {e.fullName}
                            <div className="text-muted">
                              <span className="badge badge-blue">{e.code}</span>
                            </div>
                          </td>
                          <td className="hide-sm">{e.rut}</td>
                          <td>{periodo(liq)}</td>
                          <td className="hide-sm">{formatMoney(liq?.sueldoBase ?? e.baseSalary)}</td>
                          <td>
                            {liq ? <strong>{formatMoney(liq.liquidoAPagar)}</strong> : <span className="text-muted">-</span>}
                          </td>
                          <td>
                            {liq ? (
                              <span className={'badge ' + (liq.pagada ? 'badge-green' : 'badge-yellow')}>
                                {liq.pagada ? 'Pagada' : 'Pendiente'}
                              </span>
                            ) : (
                              <span className="badge badge-gray">Sin liquidación</span>
                            )}
                          </td>
                          <td>
                            <div className="flex gap-2">
                              {liq && (
                                <button className="btn btn-sm btn-secondary" onClick={() => handleVerLiquidacion(liq)}>
                                  Ver
                                </button>
                              )}
                              {liq && !liq.pagada && (
                                <button className="btn btn-sm btn-success" onClick={() => handlePagar(liq)}>
                                  Pagar
                                </button>
                              )}
                              <button
                                className="btn btn-sm btn-info"
                                onClick={() => openHoraExtraModal(e)}
                                title="Registrar horas extra"
                              >
                                Horas extra
                              </button>
                              <button
                                className="btn btn-sm"
                                onClick={() => handleGenerateOne(e.id)}
                                disabled={saving}
                                title={`Generar y descargar PDF de la liquidación ${String(genMonth).padStart(2, '0')}/${genYear}`}
                              >
                                Generar PDF
                              </button>
                            </div>
                          </td>
                        </tr>
                      )
                    })}
                  {employees.filter((e) => !e.enabled).length > 0 && (
                    <div className="alert alert-warning mt-2">
                      {employees.filter((e) => !e.enabled).length} empleados deshabilitados no aparecen en esta lista.
                    </div>
                  )}
                </tbody>
              </table>
            )}
          </>
        )}

        {tab === 'anticipos' && (
          <>
            <div className="form-group mt-2 mb-0">
              <input
                type="text"
                className="form-control"
                placeholder="Buscar por nombre o RUT..."
                value={advSearch}
                onChange={(e) => setAdvSearch(e.target.value)}
              />
            </div>
            {employees.length === 0 ? (
              <div className="empty mt-2">No hay empleados registrados</div>
            ) : (
              <div className="mt-2">
                {employees
                  .filter((e) => {
                    const q = advSearch.trim().toLowerCase()
                    if (!q) return true
                    return (
                      e.fullName.toLowerCase().includes(q) ||
                      e.rut.toLowerCase().includes(q)
                    )
                  })
                  .map((e) => {
                    const empAdvances = advances.filter((a) => a.employeeId === e.id)
                  return (
                    <div key={e.id} className="card mt-2">
                      <div className="card-header">
                        <strong>{e.fullName}</strong> <span className="text-muted">({e.rut})</span>
                      </div>
                      {empAdvances.length === 0 ? (
                        <div className="p-3 text-muted">No hay anticipos registrados</div>
                      ) : (
                        <table className="table mb-0">
                          <thead>
                            <tr>
                              <th>Fecha</th>
                              <th>Descripción</th>
                              <th>Monto</th>
                              <th></th>
                            </tr>
                          </thead>
                          <tbody>
                            {empAdvances.map((a) => (
                              <tr key={a.id}>
                                <td>{formatDate(a.date)}</td>
                                <td>{a.description ?? '-'}</td>
                                <td>{formatMoney(a.amount)}</td>
                                <td>
                                  <button className="btn btn-sm btn-danger" onClick={() => handleDeleteAdvance(a)}>
                                    Eliminar
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                      <div className="p-2">
                        <button className="btn btn-sm btn-secondary" onClick={() => { setAdvEmployee(e); setAdvModal(true); }}>
                          + Agregar anticipo
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </>
        )}

        {tab === 'licencias' && (
          <>
            <div className="form-group mt-2 mb-0">
              <input
                type="text"
                className="form-control"
                placeholder="Buscar por nombre o RUT..."
                value={licSearch}
                onChange={(e) => setLicSearch(e.target.value)}
              />
            </div>
            {employees.length === 0 ? (
              <div className="empty mt-2">No hay empleados registrados</div>
            ) : (
              <div className="mt-2">
                {employees
                  .filter((e) => {
                    const q = licSearch.trim().toLowerCase()
                    if (!q) return true
                    return (
                      e.fullName.toLowerCase().includes(q) ||
                      e.rut.toLowerCase().includes(q)
                    )
                  })
                  .map((e) => {
                    const empLicencias = licencias.filter((l) => l.employeeId === e.id)
                  return (
                    <div key={e.id} className="card mt-2">
                      <div className="card-header">
                        <strong>{e.fullName}</strong> <span className="text-muted">({e.rut})</span>
                      </div>
                      {empLicencias.length === 0 ? (
                        <div className="p-3 text-muted">No hay licencias registradas</div>
                      ) : (
                        <table className="table mb-0">
                          <thead>
                            <tr>
                              <th>Inicio</th>
                              <th>Término</th>
                              <th>Días</th>
                              <th className="hide-sm">Motivo</th>
                              <th className="hide-sm">Paga empleador</th>
                              <th></th>
                            </tr>
                          </thead>
                          <tbody>
                            {empLicencias.map((l) => (
                              <tr key={l.id}>
                                <td>{formatDate(l.startDate)}</td>
                                <td>{formatDate(l.endDate)}</td>
                                <td>{l.totalDays}</td>
                                <td className="hide-sm">{l.motivo}</td>
                                <td className="hide-sm">{l.employerPays ? 'Sí' : 'No'}</td>
                                <td>
                                  <button className="btn btn-sm btn-danger" onClick={() => handleDeleteLicencia(l)}>
                                    Eliminar
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                      <div className="p-2">
                        <button className="btn btn-sm btn-secondary" onClick={() => { setLicEmployee(e); setLicModal(true); }}>
                          + Agregar licencia
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </>
        )}

      </div>

      {/* Modal detalle liquidación */}
      <Modal
        open={viewLiquidacion !== null}
        title="Detalle de liquidación"
        onClose={() => {
          setViewFromGenerate(false)
          setViewLiquidacion(null)
        }}
      >
        {viewLiquidacion && (
          <div>
            <p>
              <strong>{viewLiquidacion.employeeName}</strong> — {nombreMes(viewLiquidacion.periodMonth)} {viewLiquidacion.periodYear}
            </p>
            <p className="text-muted">
              Días hábiles: {viewLiquidacion.diasHabiles} · Trabajados: {viewLiquidacion.diasTrabajados} · Inasistencia: {viewLiquidacion.diasInasistencia} · Licencia: {viewLiquidacion.diasLicencia}
            </p>
            <table className="table">
              <thead>
                <tr>
                  <th>Concepto</th>
                  <th>Tipo</th>
                  <th>Monto</th>
                </tr>
              </thead>
              <tbody>
                {viewLiquidacion.detalles.map((d, i) => (
                  <tr key={i}>
                    <td>{d.concepto}</td>
                    <td>
                      <span className={'badge ' + (d.haber ? 'badge-green' : 'badge-red')}>{d.haber ? 'Haber' : 'Descuento'}</span>
                    </td>
                    <td>{formatMoney(d.monto)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="mt-2">
              <div>Total haberes: <strong>{formatMoney(viewLiquidacion.totalHaberes)}</strong></div>
              <div>Total descuentos: <strong>{formatMoney(viewLiquidacion.totalDescuentos)}</strong></div>
              <div>Líquido a pagar: <strong>{formatMoney(viewLiquidacion.liquidoAPagar)}</strong></div>
            </div>
            {viewFromGenerate && (
              <div className="mt-2">
                <button type="button" className="btn" onClick={() => handleDescargarPdf(viewLiquidacion)}>
                  Generar PDF
                </button>
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Modal resultado generar liquidaciones de todos */}
      <Modal
        open={generateAllModal}
        title="Resultado de generación"
        onClose={() => {
          setGenerateAllModal(false)
          setGenerateAllResult(null)
        }}
      >
        {generateAllResult && (
          <div className="text-center">
            <div className={`mb-3 ${generateAllResult.success ? 'text-success' : 'text-danger'}`}>
              {generateAllResult.success
                ? generateAllResult.count === 1
                  ? 'La liquidación ha sido creada con éxito'
                  : `Las ${generateAllResult.count} liquidaciones han sido creadas con éxito`
                : 'Error al generar las liquidaciones'}
            </div>
            <button className="btn" onClick={() => setGenerateAllModal(false)}>
              Aceptar
            </button>
          </div>
        )}
      </Modal>

      {/* Modal horas extra trabajador */}
      <Modal
        open={horaExtraModal.open}
        title="Registrar horas extra"
        onClose={() => setHoraExtraModal({ open: false, employee: null, date: '', horas: '', valor: '' })}
      >
        {horaExtraModal.employee && (
          <form onSubmit={(e) => { e.preventDefault(); handleSaveHoraExtra(); }}>
            <div className="form-group">
              <label>Trabajador</label>
              <input
                type="text"
                className="form-control"
                value={horaExtraModal.employee.fullName}
                readOnly
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Fecha</label>
                <input
                  type="date"
                  className="form-control"
                  value={horaExtraModal.date}
                  onChange={(e) => setHoraExtraModal({ ...horaExtraModal, date: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Horas trabajadas</label>
                <input
                  type="number"
                  min="0"
                  step="0.5"
                  className="form-control"
                  value={horaExtraModal.horas}
                  onChange={(e) => setHoraExtraModal({ ...horaExtraModal, horas: e.target.value })}
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>Valor hora (CLP)</label>
              <input
                type="number"
                min="0"
                step="any"
                className="form-control"
                value={horaExtraModal.valor}
                onChange={(e) => setHoraExtraModal({ ...horaExtraModal, valor: e.target.value })}
                required
              />
            </div>
            <div className="flex gap-2 justify-between mt-2">
              <button type="button" className="btn btn-secondary" onClick={() => setHoraExtraModal({ open: false, employee: null, date: '', horas: '', valor: '' })}>
                Cancelar
              </button>
              <button type="submit" className="btn" disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </form>
        )}
      </Modal>

      {/* Modal alta anticipo */}
      <Modal open={advModal} title="Registrar anticipo" onClose={() => { setAdvModal(false); setAdvEmployee(null); }}>
        <form onSubmit={handleAddAdvance}>
          <div className="form-row">
            <div className="form-group">
              <label>Fecha</label>
              <input type="date" className="form-control" value={advDate} onChange={(e) => setAdvDate(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Monto</label>
              <input type="number" min="0" step="any" className="form-control" value={advAmount} onChange={(e) => setAdvAmount(e.target.value)} required />
            </div>
          </div>
          <div className="form-group">
            <label>Descripción</label>
            <input className="form-control" value={advDesc} onChange={(e) => setAdvDesc(e.target.value)} />
          </div>
          {(() => {
            const emp = advEmployee
            if (!emp) return null
            const tope = Math.round(emp.baseSalary * 0.15)
            const usado = advances.filter((a) => a.employeeId === emp.id).reduce((s, a) => s + a.amount, 0)
            return (
              <div className="form-hint mt-2">
                El total de anticipos del período no puede superar el 15% del sueldo base ({formatMoney(tope)}).
                <br />
                Ya has registrado {formatMoney(usado)} en el período seleccionado.
              </div>
            )
          })()}
          <div className="flex gap-2 justify-between mt-2">
            <button type="button" className="btn btn-secondary" onClick={() => setAdvModal(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal alta licencia */}
      <Modal open={licModal} title="Registrar licencia médica" onClose={() => { setLicModal(false); setLicEmployee(null); }}>
        <form onSubmit={handleAddLicencia}>
          <div className="form-row">
            <div className="form-group">
              <label>Fecha inicio</label>
              <input type="date" className="form-control" value={licStart} onChange={(e) => setLicStart(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Fecha término</label>
              <input type="date" className="form-control" value={licEnd} onChange={(e) => setLicEnd(e.target.value)} required />
            </div>
          </div>
          <div className="form-group">
            <label>Motivo</label>
            <input className="form-control" value={licMotivo} onChange={(e) => setLicMotivo(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>
              <input type="checkbox" checked={licPays} onChange={(e) => setLicPays(e.target.checked)} /> Paga el empleador
            </label>
          </div>
          <div className="flex gap-2 justify-between mt-2">
            <button type="button" className="btn btn-secondary" onClick={() => setLicModal(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

