// Servicios de permisos laborales.
import { api } from './client'
import type {
  DevolucionRequest,
  PermisoLaboralRequest,
  PermisoLaboralResponse,
  StatusResponse,
  ValidacionRequest,
} from '../types'

export const permisosApi = {
  request: (employeeId: number, payload: PermisoLaboralRequest) =>
    api
      .post<PermisoLaboralResponse>(`/permisos/employee/${employeeId}`, payload)
      .then((r) => r.data),
  /** El admin crea un permiso a nombre de un empleado, dejándolo APROBADO. */
  createAsAdmin: (employeeId: number, payload: PermisoLaboralRequest) =>
    api
      .post<PermisoLaboralResponse>(`/permisos/admin/${employeeId}`, payload)
      .then((r) => r.data),
  listByEmployee: (employeeId: number, from?: string, to?: string) =>
    api
      .get<PermisoLaboralResponse[]>(`/permisos/employee/${employeeId}`, {
        params: { from, to },
      })
      .then((r) => r.data),
  findAll: () => api.get<PermisoLaboralResponse[]>('/permisos').then((r) => r.data),
  validate: (permisoId: number, payload: ValidacionRequest) =>
    api
      .put<PermisoLaboralResponse>(`/permisos/${permisoId}/validar`, payload)
      .then((r) => r.data),
  registerDevolucion: (permisoId: number, payload: DevolucionRequest) =>
    api
      .put<PermisoLaboralResponse>(`/permisos/${permisoId}/devolver`, payload)
      .then((r) => r.data),
  remove: (permisoId: number) =>
    api.delete<StatusResponse>(`/permisos/${permisoId}`).then((r) => r.data),
}
