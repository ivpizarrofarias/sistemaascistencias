import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  ActualizarPerfilRequest,
  AdvanceRequest,
  AdvanceResponse,
  AttendanceResponse,
  AttendanceUpdateRequest,
  AuthResponse,
  AvatarResponse,
  CambiarPasswordRequest,
  DevolucionRequest,
  EmployeeRequest,
  EmployeeResponse,
  HoraExtraRequest,
  HoraExtraResponse,
  LicenciaRequest,
  LicenciaResponse,
  LiquidacionResponse,
  LoginRequest,
  MarkRequest,
  MarkResponse,
  PermisoLaboralRequest,
  PermisoLaboralResponse,
  StatusResponse,
  ValidacionRequest,
  WeeklySchedule,
  WeeklyScheduleSave,
} from '../models/api.models';

/** La app siempre llama a rutas relativas `/api` (el proxy de Vite/Angular las redirige al backend). */
const API = '/api';

/**
 * Cliente HTTP del sistema.
 *
 * Un único servicio con un método por endpoint, para que las pantallas no
 * injecten `HttpClient` directamente.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // ==========================================================================
  // Auth
  // ==========================================================================

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/auth/login`, payload);
  }

  /** Cambia la contraseña del usuario autenticado (requiere sesión). */
  cambiarPassword(payload: CambiarPasswordRequest): Observable<void> {
    return this.http.post<void>(`${API}/auth/cambiar-password`, payload);
  }

  /** Actualiza el perfil (usuario, nombre y foto) del usuario autenticado. */
  actualizarPerfil(payload: ActualizarPerfilRequest): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(`${API}/auth/perfil`, payload);
  }

  /** Obtiene la foto de perfil de un usuario (público, para login). */
  obtenerAvatar(username: string): Observable<AvatarResponse> {
    const params = new HttpParams().set('username', username);
    return this.http.get<AvatarResponse>(`${API}/auth/avatar`, { params });
  }

  // ==========================================================================
  // Usuarios del sistema (requieren rol ADMIN)
  // ==========================================================================

  listUsers(): Observable<import('../models/api.models').SystemUser[]> {
    return this.http.get<import('../models/api.models').SystemUser[]>(`${API}/users`);
  }

  createUser(payload: import('../models/api.models').SystemUserRequest): Observable<import('../models/api.models').SystemUser> {
    return this.http.post<import('../models/api.models').SystemUser>(`${API}/users`, payload);
  }

  updateUser(id: number, payload: import('../models/api.models').SystemUserRequest): Observable<import('../models/api.models').SystemUser> {
    return this.http.put<import('../models/api.models').SystemUser>(`${API}/users/${id}`, payload);
  }

  setUserEnabled(id: number, enabled: boolean): Observable<StatusResponse> {
    const params = new HttpParams().set('enabled', enabled);
    return this.http.put<StatusResponse>(`${API}/users/${id}/enabled`, null, { params });
  }

  deleteUser(id: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/users/${id}`);
  }

  // ==========================================================================
  // Empleados (requieren rol ADMIN)
  // ==========================================================================

  listEmployees(): Observable<EmployeeResponse[]> {
    return this.http.get<EmployeeResponse[]>(`${API}/employees`);
  }

  getEmployee(id: number): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${API}/employees/${id}`);
  }

  createEmployee(payload: EmployeeRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(`${API}/employees`, payload);
  }

  updateEmployee(id: number, payload: EmployeeRequest): Observable<EmployeeResponse> {
    return this.http.put<EmployeeResponse>(`${API}/employees/${id}`, payload);
  }

  setEmployeeEnabled(id: number, enabled: boolean): Observable<StatusResponse> {
    const params = new HttpParams().set('enabled', enabled);
    return this.http.put<StatusResponse>(`${API}/employees/${id}/enabled`, null, { params });
  }

  deleteEmployee(id: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/employees/${id}`);
  }

  // ==========================================================================
  // Horarios semanales
  // ==========================================================================

  listSchedules(employeeId: number): Observable<WeeklySchedule[]> {
    return this.http.get<WeeklySchedule[]>(`${API}/schedules/${employeeId}`);
  }

  /** Si la semana no existe, el backend devuelve una plantilla vacía (id null). */
  getScheduleWeek(employeeId: number, startDate: string): Observable<WeeklySchedule> {
    const params = new HttpParams().set('startDate', startDate);
    return this.http.get<WeeklySchedule>(`${API}/schedules/${employeeId}/week`, { params });
  }

  saveSchedule(payload: WeeklyScheduleSave): Observable<WeeklySchedule> {
    return this.http.post<WeeklySchedule>(`${API}/schedules`, payload);
  }

  updateSchedule(id: number, payload: WeeklyScheduleSave): Observable<WeeklySchedule> {
    return this.http.put<WeeklySchedule>(`${API}/schedules/${id}`, payload);
  }

  deleteSchedule(id: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/schedules/${id}`);
  }

  downloadSchedulePdf(employeeId: number, startDate: string): Observable<Blob> {
    return this.http.get(`${API}/schedules/${employeeId}/week/${startDate}/pdf`, {
      responseType: 'blob',
    });
  }

  // ==========================================================================
  // Asistencias
  // ==========================================================================

  /** Marcaje por código. Es PÚBLICO (lo usa el kiosco). */
  markAttendance(payload: MarkRequest): Observable<MarkResponse> {
    return this.http.post<MarkResponse>(`${API}/attendance/mark`, payload);
  }

  /** Consulta el estado de marcaje del día. También público. */
  attendanceStatus(payload: MarkRequest): Observable<MarkResponse> {
    return this.http.post<MarkResponse>(`${API}/attendance/status`, payload);
  }

  /** Asistencias de un empleado en un rango. */
  listAttendance(employeeId: number, from?: string, to?: string): Observable<AttendanceResponse[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.get<AttendanceResponse[]>(`${API}/attendance/employee/${employeeId}`, { params });
  }

  updateAttendance(
    attendanceId: number,
    payload: AttendanceUpdateRequest,
  ): Observable<AttendanceResponse> {
    return this.http.put<AttendanceResponse>(`${API}/attendance/${attendanceId}`, payload);
  }

  setAbsence(
    employeeId: number,
    date: string,
    absence: boolean,
  ): Observable<AttendanceResponse> {
    const params = new HttpParams().set('date', date).set('absence', absence);
    return this.http.put<AttendanceResponse>(`${API}/attendance/absence/${employeeId}`, null, {
      params,
    });
  }

  // ==========================================================================
  // Permisos laborales
  // ==========================================================================

  requestPermiso(
    employeeId: number,
    payload: PermisoLaboralRequest,
  ): Observable<PermisoLaboralResponse> {
    return this.http.post<PermisoLaboralResponse>(
      `${API}/permisos/employee/${employeeId}`,
      payload,
    );
  }

  /** El admin crea el permiso a nombre de un empleado, dejándolo APROBADO. */
  createPermisoAsAdmin(
    employeeId: number,
    payload: PermisoLaboralRequest,
  ): Observable<PermisoLaboralResponse> {
    return this.http.post<PermisoLaboralResponse>(`${API}/permisos/admin/${employeeId}`, payload);
  }

  listPermisosByEmployee(
    employeeId: number,
    from?: string,
    to?: string,
  ): Observable<PermisoLaboralResponse[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.get<PermisoLaboralResponse[]>(`${API}/permisos/employee/${employeeId}`, {
      params,
    });
  }

  listPermisos(): Observable<PermisoLaboralResponse[]> {
    return this.http.get<PermisoLaboralResponse[]>(`${API}/permisos`);
  }

  validatePermiso(
    permisoId: number,
    payload: ValidacionRequest,
  ): Observable<PermisoLaboralResponse> {
    return this.http.put<PermisoLaboralResponse>(`${API}/permisos/${permisoId}/validar`, payload);
  }

  registerDevolucion(
    permisoId: number,
    payload: DevolucionRequest,
  ): Observable<PermisoLaboralResponse> {
    return this.http.put<PermisoLaboralResponse>(`${API}/permisos/${permisoId}/devolver`, payload);
  }

  deletePermiso(permisoId: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/permisos/${permisoId}`);
  }

  // ==========================================================================
  // Remuneraciones
  // ==========================================================================

  createLiquidacion(
    employeeId: number,
    year: number,
    month: number,
  ): Observable<LiquidacionResponse> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.post<LiquidacionResponse>(
      `${API}/payroll/liquidaciones/employee/${employeeId}`,
      null,
      { params },
    );
  }

  liquidarTodos(year: number, month: number): Observable<StatusResponse> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.post<StatusResponse>(`${API}/payroll/liquidaciones/todos`, null, { params });
  }

  listLiquidaciones(employeeId: number): Observable<LiquidacionResponse[]> {
    return this.http.get<LiquidacionResponse[]>(
      `${API}/payroll/liquidaciones/employee/${employeeId}`,
    );
  }

  getLiquidacion(id: number): Observable<LiquidacionResponse> {
    return this.http.get<LiquidacionResponse>(`${API}/payroll/liquidaciones/${id}`);
  }

  marcarLiquidacionPagada(id: number): Observable<StatusResponse> {
    return this.http.put<StatusResponse>(`${API}/payroll/liquidaciones/${id}/pagar`, null);
  }

  downloadLiquidacionPdf(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${API}/payroll/liquidaciones/${id}/pdf`, {
      responseType: 'blob',
      observe: 'response',
    });
  }

  addAdvance(employeeId: number, payload: AdvanceRequest): Observable<AdvanceResponse> {
    return this.http.post<AdvanceResponse>(`${API}/payroll/employee/${employeeId}/advances`, payload);
  }

  listAdvances(employeeId: number): Observable<AdvanceResponse[]> {
    return this.http.get<AdvanceResponse[]>(`${API}/payroll/employee/${employeeId}/advances`);
  }

  deleteAdvance(advanceId: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/payroll/advances/${advanceId}`);
  }

  addLicencia(employeeId: number, payload: LicenciaRequest): Observable<LicenciaResponse> {
    return this.http.post<LicenciaResponse>(
      `${API}/payroll/employee/${employeeId}/licencias`,
      payload,
    );
  }

  listLicencias(employeeId: number): Observable<LicenciaResponse[]> {
    return this.http.get<LicenciaResponse[]>(`${API}/payroll/employee/${employeeId}/licencias`);
  }

  deleteLicencia(licenciaId: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/payroll/licencias/${licenciaId}`);
  }

  listHorasExtras(employeeId: number): Observable<HoraExtraResponse[]> {
    return this.http.get<HoraExtraResponse[]>(`${API}/payroll/employee/${employeeId}/horasextras`);
  }

  addHoraExtra(employeeId: number, payload: HoraExtraRequest): Observable<HoraExtraResponse> {
    return this.http.post<HoraExtraResponse>(
      `${API}/payroll/employee/${employeeId}/horasextras`,
      payload,
    );
  }

  deleteHoraExtra(horaExtraId: number): Observable<StatusResponse> {
    return this.http.delete<StatusResponse>(`${API}/payroll/horasextras/${horaExtraId}`);
  }
}
