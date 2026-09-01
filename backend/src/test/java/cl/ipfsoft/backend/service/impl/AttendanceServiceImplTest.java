package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.attendance.MarkRequest;
import cl.ipfsoft.backend.entity.Attendance;
import cl.ipfsoft.backend.entity.Employee;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.mapper.AttendanceMapper;
import cl.ipfsoft.backend.repository.AttendanceRepository;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.LicenciaMedicaRepository;
import cl.ipfsoft.backend.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Pruebas unitarias de la máquina de estados de marcaje de asistencia.
 * Se usa un reloj fijo (miércoles) para no depender de la fecha real del sistema.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    // Reloj fijo: miércoles 26 de agosto de 2026 a las 09:00.
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-26T09:00:00Z"), ZoneId.of("America/Santiago"));

    @Mock AttendanceRepository attendanceRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock LicenciaMedicaRepository licenciaRepository;
    @Mock WeeklyScheduleRepository weeklyScheduleRepository;

    AttendanceServiceImpl service;
    Employee employee;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(
                attendanceRepository, employeeRepository, licenciaRepository,
                weeklyScheduleRepository, new AttendanceMapper(), clock);

        employee = new Employee();
        employee.setId(1L);
        employee.setRut("11111111-1");
        employee.setFirstName("Juan");
        employee.setLastName("Perez");
        employee.setEnabled(true);
        employee.setBaseSalary(new BigDecimal("1000000"));
    }

    private void mockBasics(Attendance attendance) {
        when(employeeRepository.findByRut("11111111-1")).thenReturn(Optional.of(employee));
        when(licenciaRepository.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(1L), any(), any())).thenReturn(List.of());
        lenient().when(attendanceRepository.findTopByEmployeeOrderByDateDesc(any(Employee.class)))
                .thenReturn(Optional.ofNullable(attendance));
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now(clock)))
                .thenReturn(Optional.ofNullable(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Attendance newAttendance() {
        Attendance a = new Attendance();
        a.setEmployee(employee);
        a.setDate(LocalDate.now(clock));
        a.setNextStep("ENTRADA");
        return a;
    }

    @Test
    void mark_secuenciaCompleta_avanzaPasosEnOrden() {
        // Registro compartido de hoy: las llamadas reutilizan la misma instancia.
        Attendance att = newAttendance();
        mockBasics(att);
        // findTop: no hay registro previo -> empty.
        when(attendanceRepository.findTopByEmployeeOrderByDateDesc(any(Employee.class)))
                .thenReturn(Optional.empty());

        // 1º marca -> ENTRADA.
        var r1 = service.mark(new MarkRequest("11111111-1"));
        assertNotNull(r1.entryTime());
        assertEquals("COLACION_SALIDA", r1.nextStep().name());

        // 2º marca -> SALIDA COLACIÓN.
        var r2 = service.mark(new MarkRequest("11111111-1"));
        assertNotNull(r2.colacionSalida1());
        assertEquals("COLACION_ENTRADA", r2.nextStep().name());

        // 3º marca -> ENTRADA COLACIÓN.
        var r3 = service.mark(new MarkRequest("11111111-1"));
        assertNotNull(r3.colacionEntrada1());
        assertEquals("SALIDA", r3.nextStep().name());

        // 4º marca -> SALIDA (día completo).
        var r4 = service.mark(new MarkRequest("11111111-1"));
        assertTrue(r4.complete());
        assertNull(r4.nextStep());
    }

    @Test
    void mark_empleadoInexistente_lanzaExcepcion() {
        when(employeeRepository.findByRut("11111111-1")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.mark(new MarkRequest("11111111-1")));
    }

    @Test
    void mark_empleadoDeshabilitado_lanzaExcepcion() {
        employee.setEnabled(false);
        when(employeeRepository.findByRut("11111111-1")).thenReturn(Optional.of(employee));
        assertThrows(BusinessException.class, () -> service.mark(new MarkRequest("11111111-1")));
    }

    @Test
    void mark_diaAnteriorIncompleto_quedaBloqueado() {
        // Registro anterior (ayer) incompleto -> bloquea el marcaje de hoy.
        Attendance previo = newAttendance();
        previo.setDate(LocalDate.now(clock).minusDays(1));
        previo.setComplete(false);
        previo.setNextStep("COLACION_SALIDA");

        when(employeeRepository.findByRut("11111111-1")).thenReturn(Optional.of(employee));
        when(licenciaRepository.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(1L), any(), any())).thenReturn(List.of());
        when(attendanceRepository.findTopByEmployeeOrderByDateDesc(any(Employee.class)))
                .thenReturn(Optional.of(previo));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.mark(new MarkRequest("11111111-1")));
        assertTrue(ex.getMessage().contains("incompleto"));
    }

    @Test
    void status_sinRegistroHoy_devuelveEntradaComoProximoPaso() {
        when(employeeRepository.findByRut("11111111-1")).thenReturn(Optional.of(employee));
        when(licenciaRepository.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(1L), any(), any())).thenReturn(List.of());
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now(clock)))
                .thenReturn(Optional.empty());

        var resp = service.status(new MarkRequest("11111111-1"));
        assertEquals("ENTRADA", resp.nextStep().name());
        assertNull(resp.entryTime());
    }
}
