/**
 * Modelos de datos del frontend.
 *
 * Reflejan exactamente los DTO (records) del backend Spring Boot para no
 * romper el contrato de la API. Los nombres de campo en `camelCase` coinciden
 * con el JSON que devuelve Jackson.
 */

// ============================================================================
// ENUMS
// ============================================================================

/** Rol de usuario en el sistema. */
export type Role = 'ADMIN' | 'EMPLOYEE'

/** Tipo de permiso laboral. */
export type PermisoTipo = 'POR_HORA' | 'POR_DIA'

/** Estado de una solicitud de permiso. */
export type PermisoEstado = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO'

/** Forma de compensación de un permiso. */
export type FormaCompensacion = 'DEVOLVER' | 'DESCUENTO'

/** Pasos de la secuencia de marcaje diario. */
export type MarkStep =
  | 'ENTRADA'
  | 'COLACION_SALIDA'
  | 'COLACION_ENTRADA'
  | 'SALIDA'
  | 'COMPLETE'

// ============================================================================
// AUTH
// ============================================================================

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  role: Role
  employeeId: number | null
  fullName: string | null
}

// ============================================================================
// EMPLEADO
// ============================================================================

export interface EmployeeRequest {
  rut: string
  firstName: string
  lastName: string
  phone?: string | null
  email?: string | null
  /** yyyy-MM-dd */
  hireDate: string
  baseSalary: number
  dependents: number
  pensionSystem?: string | null
  afpFund?: string | null
  healthSystem?: string | null
}

export interface EmployeeResponse {
  id: number
  rut: string
  firstName: string
  lastName: string
  fullName: string
  phone: string | null
  email: string | null
  hireDate: string
  baseSalary: number
  dependents: number
  pensionSystem: string | null
  afpFund: string | null
  healthSystem: string | null
  enabled: boolean
  /** Código de marcaje: 4 dígitos, único e inmutable. */
  code: string
}

/** Opción ligera de empleado para selectores. */
export interface EmployeeOption {
  id: number
  fullName: string
  rut: string
}

// ============================================================================
// HORARIO SEMANAL
// ============================================================================

/** Un día del horario semanal (1 = lunes ... 6 = sábado). */
export interface ScheduleDay {
  id?: number | null
  dayNumber: number
  worked: boolean
  /** HH:mm */
  startTime: string | null
  /** HH:mm */
  endTime: string | null
}

/** Horario de UNA semana de un empleado (la semana inicia en un lunes). */
export interface WeeklySchedule {
  id?: number | null
  employeeId: number
  employeeName?: string
  /** yyyy-MM-dd (lunes) */
  startDate: string
  /** yyyy-MM-dd (sábado) */
  endDate: string
  label: string
  days: ScheduleDay[]
}

export interface WeeklyScheduleSave {
  employeeId: number
  startDate: string
  days: ScheduleDay[]
}

// ============================================================================
// ASISTENCIA
// ============================================================================

export interface MarkRequest {
  /** Código de marcaje del empleado: 4 dígitos (no tiene usuario ni clave). */
  code: string
}

export interface MarkResponse {
  employeeId: number | null
  fullName: string | null
  date: string | null
  nextStep: MarkStep | null
  entryTime: string | null
  colacionSalida1: string | null
  colacionEntrada1: string | null
  colacionSalida2: string | null
  colacionEntrada2: string | null
  exitTime: string | null
  colacionSalidaCount: number
  colacionEntradaCount: number
  complete: boolean
  blocked: boolean
  message: string | null
}

export interface AttendanceResponse {
  id: number
  employeeId: number
  employeeName: string
  rut: string
  date: string
  entryTime: string | null
  colacionSalida1: string | null
  colacionEntrada1: string | null
  colacionSalida2: string | null
  colacionEntrada2: string | null
  exitTime: string | null
  complete: boolean
  absence: boolean
  medicalLicense: boolean
  adminCorrected: boolean
}

export interface AttendanceUpdateRequest {
  entryTime?: string | null
  colacionSalida1?: string | null
  colacionEntrada1?: string | null
  colacionSalida2?: string | null
  colacionEntrada2?: string | null
  exitTime?: string | null
  complete?: boolean | null
  absence?: boolean | null
  medicalLicense?: boolean | null
}

// ============================================================================
// PERMISO LABORAL
// ============================================================================

export interface PermisoLaboralRequest {
  date: string
  tipo: PermisoTipo
  horas?: number | null
  dias?: number | null
  motivo: string
  formaCompensacion: FormaCompensacion
}

export interface PermisoLaboralResponse {
  id: number
  employeeId: number
  employeeName: string
  date: string
  tipo: PermisoTipo
  horas: number | null
  dias: number
  motivo: string
  estado: PermisoEstado
  formaCompensacion: FormaCompensacion
  horasDevueltas: number | null
  diasDevueltos: number
  fechaSolicitud: string | null
  fechaValidacion: string | null
  validadoPor: string | null
  saldoPendiente: boolean
}

export interface ValidacionRequest {
  estado: PermisoEstado
  observacion?: string | null
}

export interface DevolucionRequest {
  horasDevueltas?: number | null
  diasDevueltos?: number | null
}

// ============================================================================
// REMUNERACIONES
// ============================================================================

export interface AdvanceRequest {
  date: string
  amount: number
  description?: string | null
}

export interface AdvanceResponse {
  id: number
  employeeId: number
  date: string
  amount: number
  description: string | null
}

export interface LicenciaRequest {
  startDate: string
  endDate: string
  motivo: string
}

export interface LicenciaResponse {
  id: number
  employeeId: number
  startDate: string
  endDate: string
  totalDays: number
  motivo: string
}

export interface HoraExtraRequest {
  date: string
  horas: number
  /** Monto en CLP a pagar. Es el valor exacto que se mostrará en la liquidación. */
  monto: number
}

export interface HoraExtraResponse {
  id: number
  employeeId: number
  date: string
  horas: number
  monto: number
}

export interface LiquidacionDetalleDto {
  concepto: string
  haber: boolean
  monto: number
}

export interface LiquidacionResponse {
  id: number
  employeeId: number
  employeeName: string
  rut: string
  periodYear: number
  periodMonth: number
  diasMes: number
  diasHabiles: number
  diasTrabajados: number
  diasInasistencia: number
  diasLicencia: number
  sueldoBase: number
  gratificacion: number
  asignacionFamiliar: number
  bonoMovilizacion: number
  horasExtras: number
  montoHorasExtras: number
  otrosHaberes: number
  baseImponible: number
  totalHaberes: number
  afp: number
  sis: number
  salud: number
  impuestoUnico: number
  cesantia: number
  descuentoFaltas: number
  descuentoLicenciaDias: number
  descuentoLicencia: number
  anticipos: number
  descuentoHorasExtras: number
  otrosDescuentos: number
  totalDescuentos: number
  liquidoAPagar: number
  pagada: boolean
  detalles: LiquidacionDetalleDto[]
}

// ============================================================================
// COMUNES
// ============================================================================

export interface StatusResponse {
  status: string
  message: string
  count?: number
}

/** Cuerpo de error que devuelve el backend. */
export interface ApiError {
  message: string
  status: number
  timestamp: string
  fieldErrors: Record<string, string>
}

// ============================================================================
// ETIQUETAS
// ============================================================================

export const PERMISO_TIPO_LABEL: Record<PermisoTipo, string> = {
  POR_HORA: 'Por horas',
  POR_DIA: 'Por día',
}

export const PERMISO_ESTADO_LABEL: Record<PermisoEstado, string> = {
  PENDIENTE: 'Pendiente',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
}

export const FORMA_COMPENSACION_LABEL: Record<FormaCompensacion, string> = {
  DEVOLVER: 'Devolver después',
  DESCUENTO: 'Descontar del sueldo',
}

export const MARK_STEP_LABEL: Record<MarkStep, string> = {
  ENTRADA: 'Entrada',
  COLACION_SALIDA: 'Salida a colación',
  COLACION_ENTRADA: 'Entrada de colación',
  SALIDA: 'Salida',
  COMPLETE: 'Completo',
}

/** Días de la semana en orden (índice 0 = lunes ... 5 = sábado). */
export const DIAS_SEMANA = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']
