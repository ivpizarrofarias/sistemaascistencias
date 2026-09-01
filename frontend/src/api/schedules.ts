// Servicios de horarios semanales por empleado (solo rol ADMIN).
// El horario es rotativo: se define semana a semana en una vista separada.
import { api } from './client'
import type { WeeklySchedule, StatusResponse } from '../types'

export const schedulesApi = {
  // Lista los horarios semanales de un empleado.
  listByEmployee: (employeeId: number) =>
    api.get<WeeklySchedule[]>(`/schedules/${employeeId}`).then((r) => r.data),

  // Obtiene el horario de un empleado para la semana que inicia en la fecha (lunes).
  // Si no existe, el backend devuelve una plantilla vacía (id null) lista para editar.
  getByEmployeeAndWeek: (employeeId: number, startDate: string) =>
    api
      .get<WeeklySchedule>(`/schedules/${employeeId}/week`, {
        params: { startDate },
      })
      .then((r) => r.data),

  // Crea o actualiza el horario de la semana de un empleado.
  save: (payload: unknown) =>
    api.post<WeeklySchedule>('/schedules', payload).then((r) => r.data),

  // Actualiza un horario existente por id.
  update: (id: number, payload: unknown) =>
    api.put<WeeklySchedule>(`/schedules/${id}`, payload).then((r) => r.data),

  // Elimina un horario semanal.
  remove: (id: number) => api.delete<StatusResponse>(`/schedules/${id}`).then((r) => r.data),

  /** Descarga el PDF del horario de un empleado para la semana (lunes) indicada. */
  downloadPdf: async (employeeId: number, startDate: string) => {
    const res = await api.get(`/schedules/${employeeId}/week/${startDate}/pdf`, {
      responseType: 'blob',
    })
    const blob = res.data as Blob
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `horario-${employeeId}-${startDate}.pdf`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  },
}
