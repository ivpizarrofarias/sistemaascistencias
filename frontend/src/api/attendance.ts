// Servicios de asistencia.
// mark/status son PÚBLICOS (marcaje por RUT); el resto requiere ADMIN.
import { api } from './client'
import type {
  AttendanceResponse,
  AttendanceUpdateRequest,
  MarkRequest,
  MarkResponse,
} from '../types'

export const attendanceApi = {
  mark: (payload: MarkRequest) =>
    api.post<MarkResponse>('/attendance/mark', payload).then((r) => r.data),
  status: (payload: MarkRequest) =>
    api.post<MarkResponse>('/attendance/status', payload).then((r) => r.data),
  listByEmployee: (employeeId: number, from?: string, to?: string) =>
    api
      .get<AttendanceResponse[]>(`/attendance/employee/${employeeId}`, {
        params: { from, to },
      })
      .then((r) => r.data),
  update: (attendanceId: number, payload: AttendanceUpdateRequest) =>
    api
      .put<AttendanceResponse>(`/attendance/${attendanceId}`, payload)
      .then((r) => r.data),
  markAbsence: (employeeId: number, date: string, absence: boolean) =>
    api
      .put<AttendanceResponse>(`/attendance/absence/${employeeId}`, null, {
        params: { date, absence },
      })
      .then((r) => r.data),
}
