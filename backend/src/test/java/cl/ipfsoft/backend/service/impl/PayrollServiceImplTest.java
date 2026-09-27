package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.payroll.LiquidacionResponse;
import cl.ipfsoft.backend.entity.Asistencia;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.HoraExtra;
import cl.ipfsoft.backend.entity.DiaHorario;
import cl.ipfsoft.backend.entity.ParametroSistema;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import cl.ipfsoft.backend.mapper.PayrollMapper;
import cl.ipfsoft.backend.repository.AdvanceRepository;
import cl.ipfsoft.backend.repository.AttendanceRepository;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.HoraExtraRepository;
import cl.ipfsoft.backend.repository.LicenciaMedicaRepository;
import cl.ipfsoft.backend.repository.LiquidacionRepository;
import cl.ipfsoft.backend.repository.PermisoLaboralRepository;
import cl.ipfsoft.backend.repository.SystemParamRepository;
import cl.ipfsoft.backend.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Pruebas unitarias del cálculo de la liquidación de sueldo.
 * Se utilizan repositorios simulados para aislar la lógica de cálculo
 * que sigue la legislación laboral chilena.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock AdvanceRepository advanceRepository;
    @Mock LicenciaMedicaRepository licenciaRepository;
    @Mock PermisoLaboralRepository permisoRepository;
    @Mock LiquidacionRepository liquidacionRepository;
    @Mock SystemParamRepository paramRepository;
    @Mock WeeklyScheduleRepository weeklyScheduleRepository;
    @Mock HoraExtraRepository horaExtraRepository;
    @Mock PayrollPdfService pdfService;

    PayrollServiceImpl service;
    Empleado employee;

    @BeforeEach
    void setUp() {
        service = new PayrollServiceImpl(
                employeeRepository, attendanceRepository, advanceRepository,
                licenciaRepository, permisoRepository, liquidacionRepository,
                paramRepository, weeklyScheduleRepository, horaExtraRepository,
                new PayrollMapper(), pdfService);

        employee = new Empleado();
        employee.setId(1L);
        employee.setRut("11111111-1");
        employee.setFirstName("Juan");
        employee.setLastName("Perez");

        // Horario semanal: lunes a viernes 07:30-16:30 (9h), sábado no se trabaja.
        HorarioSemanal ws = new HorarioSemanal();
        ws.setEmployee(employee);
        ws.setStartDate(LocalDate.of(2026, 8, 31));
        ws.setLabel("Semana test");
        java.util.List<DiaHorario> days = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            DiaHorario d = new DiaHorario();
            d.setSchedule(ws);
            d.setDayNumber(i);
            d.setWorked(i <= 5);
            d.setStartTime(LocalTime.of(7, 30));
            d.setEndTime(LocalTime.of(16, 30));
            days.add(d);
        }
        ws.setDays(days);
        lenient().when(weeklyScheduleRepository.findByEmployeeAndStartDate(any(Empleado.class), any(LocalDate.class)))
                .thenReturn(Optional.of(ws));

        // Por defecto no hay horas extra registradas en el período.
        lenient().when(horaExtraRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());

        employee.setBaseSalary(new BigDecimal("1000000"));
        employee.setDependents(0);
        employee.setAfpFund("APROVISIONA");
    }

    private void mockParams() {
        lenient().when(paramRepository.findById(ParametroSistema.GRATIFICACION_PCT))
                .thenReturn(Optional.of(new ParametroSistema(ParametroSistema.GRATIFICACION_PCT, "grat", new BigDecimal("0.15"))));
        lenient().when(paramRepository.findById(ParametroSistema.SIS_PCT))
                .thenReturn(Optional.of(new ParametroSistema(ParametroSistema.SIS_PCT, "sis", new BigDecimal("0.0144"))));
        lenient().when(paramRepository.findById(ParametroSistema.CESANTIA_EMPLEADO_PCT))
                .thenReturn(Optional.of(new ParametroSistema(ParametroSistema.CESANTIA_EMPLEADO_PCT, "ces", new BigDecimal("0.006"))));
        lenient().when(paramRepository.findById(ParametroSistema.UTM))
                .thenReturn(Optional.of(new ParametroSistema(ParametroSistema.UTM, "utm", new BigDecimal("67000"))));
        lenient().when(paramRepository.findById(ParametroSistema.ASIGNACION_FAMILIAR_TRAMO1))
                .thenReturn(Optional.of(new ParametroSistema(ParametroSistema.ASIGNACION_FAMILIAR_TRAMO1, "af", new BigDecimal("21569"))));
        lenient().when(paramRepository.findById(ParametroSistema.HORAS_EXTRA_MULT))
                .thenReturn(Optional.of(new ParametroSistema(ParametroSistema.HORAS_EXTRA_MULT, "hes", new BigDecimal("1.5"))));
    }

    private Asistencia attendance(LocalDate date, boolean complete, boolean absence, boolean license) {
        Asistencia a = new Asistencia();
        a.setEmployee(employee);
        a.setDate(date);
        a.setComplete(complete);
        a.setAbsence(absence);
        a.setMedicalLicense(license);
        return a;
    }

    private cl.ipfsoft.backend.entity.HoraExtra horaExtra(LocalDate date, String horas, String monto) {
        cl.ipfsoft.backend.entity.HoraExtra he = new cl.ipfsoft.backend.entity.HoraExtra();
        he.setEmployee(employee);
        he.setDate(date);
        he.setHoras(new BigDecimal(horas));
        he.setMonto(new BigDecimal(monto));
        return he;
    }

    @Test
    void createLiquidacion_plicaDiasYCalculosBasicos() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());

        // 22 días trabajados completos + 2 inasistencias.
        LocalDate d = LocalDate.of(2026, 8, 1);
        List<Asistencia> atts = new java.util.ArrayList<>();
        for (int i = 0; i < 22; i++) atts.add(attendance(d.plusDays(i), true, false, false));
        atts.add(attendance(d.plusDays(22), true, true, false));
        atts.add(attendance(d.plusDays(23), true, true, false));
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(1L, d, LocalDate.of(2026, 8, 31)))
                .thenReturn(atts);

        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        // Días contabilizados.
        assertEquals(22, l.diasTrabajados());
        assertEquals(2, l.diasInasistencia());
        assertEquals(31, l.diasMes());

        // Sueldo base: se prorratea contra los 21 días hábiles de agosto.
        // Aquí trabajó >= 21 días hábiles, así que recibe el sueldo completo.
        assertEquals(0, new BigDecimal("1000000").compareTo(l.sueldoBase()));

        // La liquidación nunca debe quedar negativa.
        assertTrue(l.liquidoAPagar().signum() >= 0);

        // El líquido es la diferencia entre haberes y descuentos.
        assertEquals(0, l.totalHaberes().subtract(l.totalDescuentos()).subtract(l.liquidoAPagar()).intValue());

        // Existen línea de detalle (haberes y descuentos).
        assertNotNull(l.detalles());
        assertTrue(l.detalles().stream().anyMatch(det -> det.haber()));
        assertTrue(l.detalles().stream().anyMatch(det -> !det.haber()));
    }

    @Test
    void createLiquidacion_incluyeAnticipoEnDescuentos() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());

        // Un anticipo de 100.000 en el período.
        cl.ipfsoft.backend.entity.Anticipo adv = new cl.ipfsoft.backend.entity.Anticipo();
        adv.setEmployee(employee);
        adv.setDate(LocalDate.of(2026, 8, 10));
        adv.setAmount(new BigDecimal("100000"));
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(adv));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(attendance(LocalDate.of(2026, 8, 1), true, false, false)));

        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        assertEquals(0, new BigDecimal("100000").compareTo(l.anticipos()));
        assertTrue(l.totalDescuentos().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void createLiquidacion_sinDiasTrabajados_noDebeDarNegativo() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());
        // Sin registros de asistencia en el período.
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        assertEquals(0, l.diasTrabajados());
        // El líquido no puede ser negativo.
        assertTrue(l.liquidoAPagar().signum() >= 0);
    }

    @Test
    void createLiquidacion_registraHorasExtraComoHaber() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(attendance(LocalDate.of(2026, 8, 1), true, false, false)));
        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Las horas extra provienen del registro por fecha del período.
        when(horaExtraRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(horaExtra(LocalDate.of(2026, 8, 10), "10", "20000")));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        assertEquals(0, new BigDecimal("10").compareTo(l.horasExtras()));
        // El monto es EXACTAMENTE el registrado: 20.000, no valorHora x horas x 1,5.
        assertEquals(0, new BigDecimal("20000").compareTo(l.montoHorasExtras()));
        // Las horas extra no llevan descuento.
        assertEquals(0, BigDecimal.ZERO.compareTo(l.descuentoHorasExtras()));
        // El monto de las horas extra suma al total de haberes.
        assertTrue(l.totalHaberes().compareTo(l.sueldoBase()) > 0);
        // Existe una línea de detalle de horas extra como haber.
        assertTrue(l.detalles().stream()
                .anyMatch(d -> d.concepto().toLowerCase().contains("horas extra") && d.haber()));
    }

    @Test
    void createLiquidacion_montoDeHorasExtraNoDependeDelValorHora() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(horaExtraRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(horaExtra(LocalDate.of(2026, 8, 3), "2.5", "10000"),
                        horaExtra(LocalDate.of(2026, 8, 12), "1.5", "10000")));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        // 2.5 + 1.5 = 4 horas, y 10.000 + 10.000 = 20.000 exactos.
        assertEquals(0, new BigDecimal("4").compareTo(l.horasExtras()));
        assertEquals(0, new BigDecimal("20000").compareTo(l.montoHorasExtras()));
    }

    @Test
    void createLiquidacion_noPagaSubsidioDeLicenciaMedica() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());

        // Licencia de 5 días: el empleador NO paga subsidio, así que los 5 días
        // quedan a cargo del trabajador y se descuentan del sueldo.
        cl.ipfsoft.backend.entity.LicenciaMedica lic = new cl.ipfsoft.backend.entity.LicenciaMedica();
        lic.setStartDate(LocalDate.of(2026, 8, 10));
        lic.setEndDate(LocalDate.of(2026, 8, 14));
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of(lic));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(attendance(LocalDate.of(2026, 8, 1), true, false, false)));
        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        // Los 5 días se descuentan completos.
        assertEquals(5, l.descuentoLicenciaDias());
        assertTrue(l.descuentoLicencia().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(l.totalDescuentos().compareTo(l.descuentoLicencia()) >= 0);
        assertTrue(l.detalles().stream()
                .anyMatch(d -> d.concepto().toLowerCase().contains("licencia m") && !d.haber()));
        // Ningún haber por subsidio de licencia médica.
        assertFalse(l.detalles().stream()
                .anyMatch(d -> d.concepto().toLowerCase().contains("subsidio") && d.haber()));
    }

    @Test
    void addAdvance_respetaTope15PorCiento() {
        // Sueldo base 1.000.000 -> 15% = 150.000.
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        // Ya hay 100.000 anticipado en el período.
        cl.ipfsoft.backend.entity.Anticipo existente = new cl.ipfsoft.backend.entity.Anticipo();
        existente.setAmount(new BigDecimal("100000"));
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(
                eq(1L), any(), any())).thenReturn(List.of(existente));

        // 100.000 + 60.000 = 160.000 > 150.000 -> debe rechazarse.
        cl.ipfsoft.backend.dto.payroll.AdvanceRequest request =
                new cl.ipfsoft.backend.dto.payroll.AdvanceRequest(
                        java.time.LocalDate.of(2026, 8, 15),
                        new BigDecimal("60000"), "anticipo");
        assertThrows(cl.ipfsoft.backend.exception.BusinessException.class,
                () -> service.addAdvance(1L, request));

        // 100.000 + 50.000 = 150.000 <= 150.000 -> debe aceptarse.
        cl.ipfsoft.backend.dto.payroll.AdvanceRequest ok =
                new cl.ipfsoft.backend.dto.payroll.AdvanceRequest(
                        java.time.LocalDate.of(2026, 8, 16),
                        new BigDecimal("50000"), "anticipo");
        when(advanceRepository.save(any(cl.ipfsoft.backend.entity.Anticipo.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        assertNotNull(service.addAdvance(1L, ok));
    }

    @Test
    void createLiquidacion_sumaHorasExtraRegistradasEnElPeriodo() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());

        // El empleado registró 2 horas extra por $10.000 el 29-08 y 1,5 h por $10.000 el 30-08.
        when(horaExtraRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(
                        horaExtra(LocalDate.of(2026, 8, 29), "2", "10000"),
                        horaExtra(LocalDate.of(2026, 8, 30), "1.5", "10000")));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of(attendance(LocalDate.of(2026, 8, 1), true, false, false)));
        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        // Total de horas extra registradas en el período (2 + 1,5 = 3,5).
        assertEquals(0, new BigDecimal("3.5").compareTo(l.horasExtras()));
        // El monto es la suma exacta de lo registrado: 10.000 + 10.000 = 20.000.
        assertEquals(0, new BigDecimal("20000").compareTo(l.montoHorasExtras()));
        assertTrue(l.totalHaberes().compareTo(l.sueldoBase()) > 0);
        assertTrue(l.detalles().stream()
                .anyMatch(d -> d.concepto().toLowerCase().contains("horas extra") && d.haber()));
    }

    @Test
    void createLiquidacion_visibilizaDescuentoPorInasistencia() {
        mockParams();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(1L, 2026, 8))
                .thenReturn(Optional.empty());
        when(advanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(licenciaRepository.findByEmployeeId(1L)).thenReturn(List.of());
        when(horaExtraRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
                .thenReturn(List.of());

        // Agosto 2026 tiene 21 días hábiles. Asiste completo los primeros 20 hábiles
        // y el 21º (viernes 28-08) es inasistencia.
        List<Asistencia> atts = new java.util.ArrayList<>();
        int[] habiles = {3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 17, 18, 19, 20, 21, 24, 25, 26, 27, 28};
        for (int day : habiles) atts.add(attendance(LocalDate.of(2026, 8, day), true, false, false));
        atts.add(attendance(LocalDate.of(2026, 8, 31), true, true, false));
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))).thenReturn(atts);

        when(liquidacionRepository.save(any(cl.ipfsoft.backend.entity.Liquidacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LiquidacionResponse l = service.createLiquidacion(1L, 2026, 8);

        assertEquals(20, l.diasTrabajados());
        assertEquals(1, l.diasInasistencia());
        assertEquals(21, l.diasHabiles());
        // El sueldo base se muestra completo.
        assertEquals(0, new BigDecimal("1000000").compareTo(l.sueldoBase()));
        // Existe la línea visible de descuento por inasistencia.
        assertTrue(l.descuentoFaltas().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(l.detalles().stream()
                .anyMatch(d -> d.concepto().toLowerCase().contains("inasistencia") && !d.haber()));
    }

    @Test
    void createLiquidacion_empleadoDeshabilitado_lanzaError() {
        employee.setEnabled(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(cl.ipfsoft.backend.exception.BusinessException.class,
                () -> service.createLiquidacion(1L, 2026, 8));
    }
}
