// Servicios de remuneraciones (liquidaciones, anticipos y licencias médicas).
import { api } from './client'
import type {
  AdvanceRequest,
  AdvanceResponse,
  HoraExtraRequest,
  HoraExtraResponse,
  LicenciaRequest,
  LicenciaResponse,
  LiquidacionResponse,
  StatusResponse,
} from '../types'

export const payrollApi = {
  createLiquidacion: (employeeId: number, year: number, month: number, horasExtras?: number) =>
    api
      .post<LiquidacionResponse>(`/payroll/liquidaciones/employee/${employeeId}`, null, {
        params: { year, month, horasExtras },
      })
      .then((r) => r.data),
  liquidarTodos: (year: number, month: number) =>
    api
      .post<StatusResponse>('/payroll/liquidaciones/todos', null, { params: { year, month } })
      .then((r) => r.data),
  listLiquidaciones: (employeeId: number) =>
    api
      .get<LiquidacionResponse[]>(`/payroll/liquidaciones/employee/${employeeId}`)
      .then((r) => r.data),
  getLiquidacion: (id: number) =>
    api.get<LiquidacionResponse>(`/payroll/liquidaciones/${id}`).then((r) => r.data),
  marcarPagada: (id: number) =>
    api.put<StatusResponse>(`/payroll/liquidaciones/${id}/pagar`).then((r) => r.data),
  /** Descarga el PDF de una liquidación (abre el archivo en una pestaña/hace descarga). */
  downloadLiquidacionPdf: async (id: number) => {
    const res = await api.get(`/payroll/liquidaciones/${id}/pdf`, { responseType: 'blob' })
    const blob = res.data as Blob
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `liquidacion-${id}.pdf`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  },
  addAdvance: (employeeId: number, payload: AdvanceRequest) =>
    api
      .post<AdvanceResponse>(`/payroll/employee/${employeeId}/advances`, payload)
      .then((r) => r.data),
  listAdvances: (employeeId: number) =>
    api.get<AdvanceResponse[]>(`/payroll/employee/${employeeId}/advances`).then((r) => r.data),
  deleteAdvance: (advanceId: number) =>
    api.delete<StatusResponse>(`/payroll/advances/${advanceId}`).then((r) => r.data),
  addLicencia: (employeeId: number, payload: LicenciaRequest) =>
    api
      .post<LicenciaResponse>(`/payroll/employee/${employeeId}/licencias`, payload)
      .then((r) => r.data),
  listLicencias: (employeeId: number) =>
    api.get<LicenciaResponse[]>(`/payroll/employee/${employeeId}/licencias`).then((r) => r.data),
  deleteLicencia: (licenciaId: number) =>
    api.delete<StatusResponse>(`/payroll/licencias/${licenciaId}`).then((r) => r.data),
  listHorasExtras: (employeeId: number) =>
    api.get<HoraExtraResponse[]>(`/payroll/employee/${employeeId}/horasextras`).then((r) => r.data),
  addHoraExtra: (employeeId: number, payload: HoraExtraRequest) =>
    api
      .post<HoraExtraResponse>(`/payroll/employee/${employeeId}/horasextras`, payload)
      .then((r) => r.data),
  deleteHoraExtra: (horaExtraId: number) =>
    api.delete<StatusResponse>(`/payroll/horasextras/${horaExtraId}`).then((r) => r.data),
}
