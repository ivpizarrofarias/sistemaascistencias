// Servicios de empleados (requieren rol ADMIN).
import { api } from './client'
import type { EmployeeRequest, EmployeeResponse, StatusResponse } from '../types'

export const employeesApi = {
  findAll: () => api.get<EmployeeResponse[]>('/employees').then((r) => r.data),
  findById: (id: number) => api.get<EmployeeResponse>(`/employees/${id}`).then((r) => r.data),
  create: (payload: EmployeeRequest) =>
    api.post<EmployeeResponse>('/employees', payload).then((r) => r.data),
  update: (id: number, payload: EmployeeRequest) =>
    api.put<EmployeeResponse>(`/employees/${id}`, payload).then((r) => r.data),
  setEnabled: (id: number, enabled: boolean) =>
    api.put<StatusResponse>(`/employees/${id}/enabled`, null, { params: { enabled } }).then((r) => r.data),
  remove: (id: number) => api.delete<StatusResponse>(`/employees/${id}`).then((r) => r.data),
}
