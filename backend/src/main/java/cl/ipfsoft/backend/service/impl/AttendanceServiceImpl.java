package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.attendance.AttendanceResponse;
import cl.ipfsoft.backend.dto.attendance.AttendanceUpdateRequest;
import cl.ipfsoft.backend.dto.attendance.MarkRequest;
import cl.ipfsoft.backend.dto.attendance.MarkResponse;
import cl.ipfsoft.backend.entity.Asistencia;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.LicenciaMedica;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.exception.ResourceNotFoundException;
import cl.ipfsoft.backend.mapper.AttendanceMapper;
import cl.ipfsoft.backend.repository.AttendanceRepository;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.LicenciaMedicaRepository;
import cl.ipfsoft.backend.repository.WeeklyScheduleRepository;
import cl.ipfsoft.backend.service.AttendanceService;
import cl.ipfsoft.backend.util.RutUtils;
import cl.ipfsoft.backend.util.ScheduleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementación del servicio de asistencia.
 *
 * Contiene la máquina de estados de marcaje que impone el orden estricto:
 *   ENTRADA -> SALIDA COLACIÓN -> ENTRADA COLACIÓN -> SALIDA (una única colación)
 *
 * Regla de bloqueo (norma del negocio): si el empleado NO completa un día
 * (por ejemplo marca solo la entrada y no las horas de colación ni la salida),
 * entonces NO puede marcar ese día ni el día siguiente, hasta que un administrador
 * corrija el registro (marcar como completo o editar los tiempos).
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LicenciaMedicaRepository licenciaRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final AttendanceMapper mapper;
    private final Clock clock;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 EmployeeRepository employeeRepository,
                                 LicenciaMedicaRepository licenciaRepository,
                                 WeeklyScheduleRepository weeklyScheduleRepository,
                                 AttendanceMapper mapper,
                                 Clock clock) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.licenciaRepository = licenciaRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ========================================================================
    // MARCAJE SELF-SERVICE (kiosco / móvil)
    // ========================================================================

    /**
     * Registra la siguiente marca de la secuencia para el empleado indicado.
     * Si el empleado tiene un día previo sin completar, queda bloqueado.
     */
    @Override
    @Transactional
    public MarkResponse mark(MarkRequest request) {
        // 1. Localizar y validar al empleado por su RUT (código).
        Empleado employee = resolveEmployee(request.code());

        LocalDate today = LocalDate.now(clock);

        // 2. Si el empleado está en licencia médica hoy, no puede marcar.
        if (isOnMedicalLicense(employee, today)) {
            ensureLicenseAttendance(employee, today);
            return blocked("Usted está en licencia médica el día de hoy.");
        }

        // 3. Regla del sábado según el horario del empleado: si hoy es sábado y su
        //    horario semanal indica que no lo trabaja, no corresponde marcar.
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY && !trabajaSabado(employee, today)) {
            return blocked("Hoy es sábado y su horario no considera trabajo ese día.");
        }

        // 4. Regla de bloqueo por día anterior incompleto.
        //    Si el último registro del empleado es de un día ANTERIOR y quedó incompleto,
        //    este empleado NO puede marcar hasta que el administrador lo corrija.
        attendanceRepository.findTopByEmployeeOrderByDateDesc(employee).ifPresent(latest -> {
            if (latest.getDate().isBefore(today)
                    && !latest.isComplete()
                    && !latest.isAbsence()
                    && !latest.isMedicalLicense()) {
                throw new BusinessException("No puede marcar asistencia: tiene el día "
                        + latest.getDate() + " incompleto. Solicite la corrección al administrador.");
            }
        });

        // 5. Buscar o crear el registro de asistencia de HOY.
        Asistencia attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), today)
                .orElseGet(() -> newAttendance(employee, today));

        // 6. Avanzar la secuencia según el estado actual.
        return applyNextMark(attendance);
    }

    /**
     * Consulta el estado actual sin registrar ninguna marca.
     */
    @Override
    @Transactional(readOnly = true)
    public MarkResponse status(MarkRequest request) {
        Empleado employee = resolveEmployee(request.code());
        LocalDate today = LocalDate.now(clock);

        // Si está en licencia hoy.
        if (isOnMedicalLicense(employee, today)) {
            return blocked("Usted está en licencia médica el día de hoy.");
        }

        // Recuperar el registro de hoy (si existe).
        Asistencia attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), today).orElse(null);

        // Si no existe registro de hoy, el proximo paso es la ENTRADA del trabajo.
        if (attendance == null) {
            Asistencia empty = newAttendance(employee, today);
            return mapper.toMarkResponse(empty);
        }

        return mapper.toMarkResponse(attendance);
    }

    /**
     * Aplica la marca correspondiente al próximo paso permitido de la secuencia.
     */
    private MarkResponse applyNextMark(Asistencia a) {
        LocalTime now = LocalTime.now(clock);
        String step = a.getNextStep();

        // Si no hay siguiente paso, el día ya está completo.
        if (step == null || "COMPLETE".equals(step)) {
            return done("Su asistencia de hoy ya está completa. Gracias.");
        }

        switch (step) {
            case "ENTRADA" -> {
                // Registrar la entrada del trabajo.
                a.setEntryTime(now);
                // El siguiente paso es la salida a colación.
                a.setNextStep("COLACION_SALIDA");
            }
            case "COLACION_SALIDA" -> {
                // El trabajador tiene UNA única colación: se registra su salida.
                a.setColacionSalida1(now);
                a.setColacionSalidaCount(1);
                // El siguiente paso es la entrada de colación.
                a.setNextStep("COLACION_ENTRADA");
            }
            case "COLACION_ENTRADA" -> {
                // Se registra la entrada de la (única) colación.
                a.setColacionEntrada1(now);
                a.setColacionEntradaCount(1);
                // Después de la colación, el siguiente paso es la salida del trabajo.
                a.setNextStep("SALIDA");
            }
            case "SALIDA" -> {
                // Registrar la salida del trabajo -> día COMPLETO.
                // nextStep queda null porque ya no hay más pasos para hoy:
                // el empleado solo podrá marcar la entrada del siguiente día.
                a.setExitTime(now);
                a.setComplete(true);
                a.setNextStep(null);
            }
            default -> throw new BusinessException("Estado de asistencia no reconocido: " + step);
        }

        // Guardar los cambios.
        attendanceRepository.save(a);
        return mapper.toMarkResponse(a);
    }

    // ========================================================================
    // GESTIÓN DEL ADMINISTRADOR
    // ========================================================================

    /** Lista las asistencias de un empleado en el rango indicado. */
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> listByEmployee(Long employeeId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now(clock).withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now(clock);
        return attendanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * El administrador corrige/completa una asistencia.
     * Al marcarla como "complete" (o al fijar la salida), se DESBLOQUEA al empleado
     * para que pueda seguir marcando normalmente.
     */
    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long attendanceId, AttendanceUpdateRequest request) {
        Asistencia a = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada con id " + attendanceId));

        // Aplicar solo los campos que vienen con valor (los null se ignoran).
        if (request.entryTime() != null) a.setEntryTime(request.entryTime());
        if (request.colacionSalida1() != null) a.setColacionSalida1(request.colacionSalida1());
        if (request.colacionEntrada1() != null) a.setColacionEntrada1(request.colacionEntrada1());
        if (request.colacionSalida2() != null) a.setColacionSalida2(request.colacionSalida2());
        if (request.colacionEntrada2() != null) a.setColacionEntrada2(request.colacionEntrada2());
        if (request.exitTime() != null) a.setExitTime(request.exitTime());
        // Si se marca como completa, se recalcula el conteo de colaciones.
        if (Boolean.TRUE.equals(request.complete())) {
            a.setComplete(true);
            a.setNextStep("COMPLETE");
            a.setAdminCorrected(true);
        }
        // Si se desmarca como completa, el administrador reactiva la secuencia.
        if (Boolean.FALSE.equals(request.complete())) {
            a.setComplete(false);
            a.setNextStep(a.getExitTime() == null ? "SALIDA" : "ENTRADA");
            a.setAdminCorrected(true);
        }

        // Marcar ausencia o licencia (el día no se cuenta como trabajado).
        if (request.absence() != null) {
            a.setAbsence(request.absence());
            a.setComplete(true);
        }
        if (request.medicalLicense() != null) {
            a.setMedicalLicense(request.medicalLicense());
            a.setComplete(true);
        }

        attendanceRepository.save(a);
        return mapper.toResponse(a);
    }

    /** Marca un día como ausencia no justificada (descontará del sueldo en la liquidación). */
    @Override
    @Transactional
    public AttendanceResponse markAbsence(Long employeeId, LocalDate date, boolean absence) {
        Empleado employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + employeeId));

        Asistencia a = attendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElseGet(() -> newAttendance(employee, date));
        a.setAbsence(absence);
        a.setComplete(true);
        if (!absence) {
            a.setAbsence(false);
            // Si se anula la ausencia, el día vuelve a estar pendiente de marcaje.
            a.setComplete(false);
            a.setNextStep("ENTRADA");
        }
        attendanceRepository.save(a);
        return mapper.toResponse(a);
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Busca al empleado por su código de marcaje y comprueba que esté habilitado.
     *
     * El código son 4 dígitos, así que se eliminan los espacios que el trabajador
     * pueda teclear. Si el texto no tiene ese formato se usa como respaldo el RUT
     * chileno (compatibilidad con registros antiguos), pero la credencial oficial
     * del trabajador es el código generado por el sistema.
     */
    private Empleado resolveEmployee(String code) {
        String key = code.trim();

        // 1. Intentar por el código de marcaje.
        Empleado employee = employeeRepository.findByCode(key).orElse(null);

        // 2. Respaldo: si no es un código, probar como RUT chileno normalizado.
        if (employee == null) {
            try {
                employee = employeeRepository.findByRut(RutUtils.normalize(key)).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // El texto no es un RUT válido: simplemente no se encontró al empleado.
            }
        }

        if (employee == null) {
            throw new BusinessException("Empleado no encontrado con el código " + code, 404);
        }
        if (!employee.isEnabled()) {
            throw new BusinessException("Empleado deshabilitado. Contacte al administrador.");
        }
        return employee;
    }

    /** Crea un registro de asistencia nuevo para el empleado en la fecha indicada. */
    private Asistencia newAttendance(Empleado employee, LocalDate date) {
        Asistencia a = new Asistencia();
        a.setEmployee(employee);
        a.setDate(date);
        a.setNextStep("ENTRADA");
        return a;
    }

    /**
     * Indica si, según el horario de la semana de la fecha indicada, el empleado
     * trabaja el sábado. Si no tiene un horario definido para esa semana se asume
     * que sí (no se le bloquea el marcaje).
     */
    private boolean trabajaSabado(Empleado employee, LocalDate date) {
        HorarioSemanal ws = weeklyScheduleRepository
                .findByEmployeeAndStartDate(employee, ScheduleUtils.mondayOf(date))
                .orElse(null);
        return ScheduleUtils.isDayWorked(ws, 6, true);
    }

    /** Indica si el empleado tiene una licencia médica activa en la fecha indicada. */
    private boolean isOnMedicalLicense(Empleado employee, LocalDate date) {
        return !licenciaRepository
                .findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employee.getId(), date, date)
                .isEmpty();
    }

    /** Crea/marca el registro de asistencia de hoy como licencia médica. */
    private void ensureLicenseAttendance(Empleado employee, LocalDate date) {
        attendanceRepository.findByEmployeeIdAndDate(employee.getId(), date).ifPresentOrElse(a -> {
            if (!a.isMedicalLicense()) {
                a.setMedicalLicense(true);
                a.setComplete(true);
                attendanceRepository.save(a);
            }
        }, () -> {
            Asistencia a = newAttendance(employee, date);
            a.setMedicalLicense(true);
            a.setComplete(true);
            a.setNextStep("COMPLETE");
            attendanceRepository.save(a);
        });
    }

    /** Construye una respuesta de estado bloqueado (sin excepción). */
    private MarkResponse blocked(String message) {
        return new MarkResponse(null, null, LocalDate.now(clock), null, null, null, null, null, null,
                null, 0, 0, false, true, message);
    }

    /** Construye una respuesta de día completado. */
    private MarkResponse done(String message) {
        return new MarkResponse(null, null, LocalDate.now(clock), null, null, null, null, null, null,
                null, 0, 0, true, false, message);
    }
}
