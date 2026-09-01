// Página de gestión de empleados (solo administrador).
import { useCallback, useEffect, useState } from 'react'
import { employeesApi } from '../api/employees'
import type { EmployeeResponse } from '../types'
import { formatMoney } from '../types'
import { EmployeeForm } from '../components/EmployeeForm'
import { Modal } from '../components/Modal'

export function EmployeesPage() {
  const [employees, setEmployees] = useState<EmployeeResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<EmployeeResponse | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<EmployeeResponse | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setEmployees(await employeesApi.findAll())
      setError('')
    } catch {
      setError('No se pudieron cargar los empleados')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const openCreate = () => {
    setEditing(null)
    setModalOpen(true)
  }

  const openEdit = (emp: EmployeeResponse) => {
    setEditing(emp)
    setModalOpen(true)
  }

  const handleSaved = async () => {
    setModalOpen(false)
    await load()
  }

  const handleToggleEnabled = async (emp: EmployeeResponse) => {
    await employeesApi.setEnabled(emp.id, !emp.enabled)
    await load()
  }

  const handleDelete = (emp: EmployeeResponse) => {
    setDeleteTarget(emp)
  }

  const confirmDelete = async () => {
    if (!deleteTarget) return
    await employeesApi.remove(deleteTarget.id)
    setDeleteTarget(null)
    await load()
  }

  return (
    <div>
      <div className="page-header">
        <h2>Empleados</h2>
        <button className="btn" onClick={openCreate}>
          + Nuevo empleado
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading">Cargando...</div>
      ) : (
        <div className="card">
          {employees.length === 0 ? (
            <div className="empty">No hay empleados registrados</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>RUT</th>
                  <th>Nombre</th>
                  <th>Código</th>
                  <th className="hide-sm">Sueldo base</th>
                  <th className="hide-sm">Cargas</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {employees.map((e) => (
                  <tr key={e.id}>
                    <td>{e.rut}</td>
                    <td>
                      {e.fullName}
                      <div className="text-muted">{e.email ?? ''}</div>
                    </td>
                    <td>
                      <span className="badge badge-blue">{e.code}</span>
                    </td>
                    <td className="hide-sm">{formatMoney(e.baseSalary)}</td>
                    <td className="hide-sm">{e.dependents}</td>
                    <td>
                      <span className={'badge ' + (e.enabled ? 'badge-green' : 'badge-red')}>
                        {e.enabled ? 'Habilitado' : 'Deshabilitado'}
                      </span>
                    </td>
                    <td>
                      <div className="flex gap-2">
                        <button className="btn btn-sm btn-secondary" onClick={() => openEdit(e)}>
                          Editar
                        </button>
                        <button className="btn btn-sm btn-warning" onClick={() => handleToggleEnabled(e)}>
                          {e.enabled ? 'Deshabilitar' : 'Habilitar'}
                        </button>
                        <button className="btn btn-sm btn-danger" onClick={() => handleDelete(e)}>
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

      <Modal open={modalOpen} title={editing ? 'Editar empleado' : 'Nuevo empleado'} onClose={() => setModalOpen(false)}>
        <EmployeeForm
          initial={editing}
          submitLabel={editing ? 'Actualizar' : 'Crear'}
          onCancel={() => setModalOpen(false)}
          onSubmit={async (data) => {
            if (editing) {
              await employeesApi.update(editing.id, data)
            } else {
              await employeesApi.create(data)
            }
            await handleSaved()
          }}
        />
      </Modal>

      <Modal open={deleteTarget !== null} title="Eliminar trabajador" onClose={() => setDeleteTarget(null)}>
        {deleteTarget && (
          <div>
            <p>
              ¿Realmente desea eliminar al trabajador <strong>{deleteTarget.fullName}</strong>?
            </p>
            <p className="text-muted">Esta acción no se puede deshacer.</p>
            <div className="flex gap-2 justify-between mt-2">
              <button type="button" className="btn btn-secondary" onClick={() => setDeleteTarget(null)}>
                Cancelar
              </button>
              <button type="button" className="btn btn-danger" onClick={confirmDelete}>
                Eliminar
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}