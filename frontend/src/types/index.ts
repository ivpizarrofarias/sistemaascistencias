// Tipos de datos compartidos del frontend.
// Reflejan los DTO (records) del backend Spring Boot para mantener los contratos
// de la API exactos en ambos lados.

// ============================================================================
// ENUMS
// ============================================================================

/** Rol de usuario en el sistema. */
export type Role = 'ADMIN' | 'EMPLOYEE'

/** Tipo de permiso laboral. */
export type PermisoTipo = 'POR_HORA' | 'POR_DIA'

export type PermisoTipoLabel = Record<PermisoTipo, string>

/** Estado de una solicitud de permiso. */
export type PermisoEstado = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO'

export type PermisoEstadoLabel = Record<PermisoEstado, string>

/** Forma de compensación de un permiso. */
export type FormaCompensacion = 'DEVOLVER' | 'DESCUENTO'

export type FormaCompensacionLabel = Record<FormaCompensacion, string>

/** Pasos de la secuencia de marcaje diario. */
export type MarkStep =
  | 'ENTRADA'
  | 'COLACION_SALIDA'
  | 'COLACION_ENTRADA'
  | 'SALIDA'
  | 'COMPLETE'

export type MarkStepLabel = Record<MarkStep, string>

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
// EMPLOYEE
// ============================================================================

export interface EmployeeRequest {
  rut: string
  firstName: string
  lastName: string
  phone?: string | null
  email?: string | null
  hireDate: string // yyyy-MM-dd
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
  // Código de marcaje generado automáticamente (el empleado no tiene usuario ni clave).
  code: string
}

// ============================================================================
// SCHEDULE (horario semanal rotativo por empleado)
// ============================================================================

/** Un día del horario semanal (1=lunes ... 6=sábado). */
export interface ScheduleDay {
  id?: number | null
  dayNumber: number
  worked: boolean
  startTime: string | null // HH:mm
  endTime: string | null // HH:mm
}

/** Horario de UNA semana de un empleado (la semana inicia en un lunes). */
export interface WeeklySchedule {
  id?: number | null
  employeeId: number
  employeeName?: string
  startDate: string // yyyy-MM-dd (lunes)
  endDate: string // yyyy-MM-dd (sábado)
  label: string
  days: ScheduleDay[]
}

/** Nombres de los días de la semana (índice 0 = lunes ... 5 = sábado). */
export const DIAS_SEMANA = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']

// ============================================================================
// ATTENDANCE
// ============================================================================

export interface MarkRequest {
  code: string // Código de marcaje del empleado (no tiene usuario ni clave).
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
// PAYROLL
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
  employerPays: boolean
}

export interface LicenciaResponse {
  id: number
  employeeId: number
  startDate: string
  endDate: string
  totalDays: number
  motivo: string
  employerPays: boolean
}

export interface HoraExtraRequest {
  date: string
  horas: number
}

export interface HoraExtraResponse {
  id: number
  employeeId: number
  date: string
  horas: number
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
  subsidioLicenciaEmpleador: number
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
// COMMON
// ============================================================================

export interface StatusResponse {
  status: string
  message: string
  count?: number
}

export interface ApiError {
  message: string
  status: number
  timestamp: string
  fieldErrors: Record<string, string>
}

// ============================================================================
// HELPERS / ETIQUETAS
// ============================================================================

/** Etiquetas y helpers de los permisos laborales. */
export const PermisoTipoLabel: PermisoTipoLabel = {
  POR_HORA: 'Por horas',
  POR_DIA: 'Por día',
}

export const PermisoEstadoLabel: PermisoEstadoLabel = {
  PENDIENTE: 'Pendiente',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
}

export const FormaCompensacionLabel: FormaCompensacionLabel = {
  DEVOLVER: 'Devolver después',
  DESCUENTO: 'Descontar del sueldo',
}

export const MarkStepLabel: MarkStepLabel = {
  ENTRADA: 'Entrada',
  COLACION_SALIDA: 'Salida a colación',
  COLACION_ENTRADA: 'Entrada de colación',
  SALIDA: 'Salida',
  COMPLETE: 'Completo',
}

export interface EmployeeOption {
  id: number
  fullName: string
  rut: string
}

export const formatMoney = (value: number | null | undefined): string => {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0,
  }).format(value)
}

export const formatDate = (value: string | null | undefined): string => {
  if (!value) return '-'
  return new Date(value + (value.length === 10 ? 'T00:00:00' : '')).toLocaleDateString('es-CL')
}

export const formatTime = (value: string | null | undefined): string => {
  if (!value) return '-'
  return value.slice(0, 5)
}
