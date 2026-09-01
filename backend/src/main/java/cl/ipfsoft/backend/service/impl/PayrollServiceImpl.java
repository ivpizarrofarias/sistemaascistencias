package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.payroll.AdvanceRequest;
import cl.ipfsoft.backend.dto.payroll.AdvanceResponse;
import cl.ipfsoft.backend.dto.payroll.HoraExtraRequest;
import cl.ipfsoft.backend.dto.payroll.HoraExtraResponse;
import cl.ipfsoft.backend.dto.payroll.LicenciaRequest;
import cl.ipfsoft.backend.dto.payroll.LicenciaResponse;
import cl.ipfsoft.backend.dto.payroll.LiquidacionResponse;
import cl.ipfsoft.backend.entity.Advance;
import cl.ipfsoft.backend.entity.Attendance;
import cl.ipfsoft.backend.entity.Employee;
import cl.ipfsoft.backend.entity.FormaCompensacion;
import cl.ipfsoft.backend.entity.HoraExtra;
import cl.ipfsoft.backend.entity.LicenciaMedica;
import cl.ipfsoft.backend.entity.Liquidacion;
import cl.ipfsoft.backend.entity.LiquidacionDetalle;
import cl.ipfsoft.backend.entity.PermisoEstado;
import cl.ipfsoft.backend.entity.PermisoLaboral;
import cl.ipfsoft.backend.entity.PermisoTipo;
import cl.ipfsoft.backend.entity.SystemParam;
import cl.ipfsoft.backend.entity.WeeklySchedule;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.exception.ResourceNotFoundException;
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
import cl.ipfsoft.backend.service.PayrollService;
import cl.ipfsoft.backend.util.ScheduleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Implementación del servicio de remuneraciones (liquidación de sueldo).
 *
 * El cálculo sigue las normas generales de la liquidación chilena:
 *  - Haberes: sueldo base, gratificación legal, asignación familiar por cargas,
 *    subsidio por licencia médica (días a cargo del empleador).
 *  - Descuentos: AFP (10%), Seguro de Invalidez y Sobrevivencia, salud (7%),
 *    seguro de cesantía, Impuesto Único de 2ª categoría, descuento por faltas,
 *    anticipos y permisos laborales.
 *  - La base imponible y los valores se toman de los parámetros del sistema.
 */
@Service
public class PayrollServiceImpl implements PayrollService {

    // Tasas fijas legales chilenas:
    private static final BigDecimal AFP_PCT = new BigDecimal("0.10");      // 10% cotización previsional
    private static final BigDecimal SALUD_PCT = new BigDecimal("0.07");    // 7% cotización de salud
    private static final BigDecimal DIA_LICENCIA_EMPLEADOR = new BigDecimal("3"); // hasta 3 días empleador
    private static final BigDecimal DESCUENTO_HORAS_EXTRAS_PCT = BigDecimal.ZERO; // Sin descuento por horas extras

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AdvanceRepository advanceRepository;
    private final LicenciaMedicaRepository licenciaRepository;
    private final PermisoLaboralRepository permisoRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final SystemParamRepository paramRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final HoraExtraRepository horaExtraRepository;
    private final PayrollMapper mapper;
    private final PayrollPdfService pdfService;

    public PayrollServiceImpl(EmployeeRepository employeeRepository,
                              AttendanceRepository attendanceRepository,
                              AdvanceRepository advanceRepository,
                              LicenciaMedicaRepository licenciaRepository,
                              PermisoLaboralRepository permisoRepository,
                              LiquidacionRepository liquidacionRepository,
                              SystemParamRepository paramRepository,
                              WeeklyScheduleRepository weeklyScheduleRepository,
                              HoraExtraRepository horaExtraRepository,
                              PayrollMapper mapper,
                              PayrollPdfService pdfService) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.advanceRepository = advanceRepository;
        this.licenciaRepository = licenciaRepository;
        this.permisoRepository = permisoRepository;
        this.liquidacionRepository = liquidacionRepository;
        this.paramRepository = paramRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.horaExtraRepository = horaExtraRepository;
        this.mapper = mapper;
        this.pdfService = pdfService;
    }

    // ========================================================================
    // CÁLCULO DE LA LIQUIDACIÓN
    // ========================================================================

    /**
     * Calcula, guarda (o recalcula) la liquidación de un empleado para el período.
     * Si ya existía una liquidación para el período, se reemplaza.
     */
    @Override
    @Transactional
    public LiquidacionResponse createLiquidacion(Long employeeId, int year, int month) {
        return createLiquidacion(employeeId, year, month, BigDecimal.ZERO);
    }

    /**
     * Igual que {@link #createLiquidacion(Long, int, int)} pero permite registrar
     * las horas extra del período (campo manual del administrador).
     */
    @Override
    @Transactional
    public LiquidacionResponse createLiquidacion(Long employeeId, int year, int month, BigDecimal horasExtras) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + employeeId));
        if (!employee.isEnabled()) {
            throw new BusinessException("El empleado está deshabilitado y no se le puede generar la liquidación de sueldo.");
        }
        YearMonth period = YearMonth.of(year, month);
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        // Reemplazar liquidación previa del período si existiera.
        liquidacionRepository.findByEmployeeIdAndPeriodYearAndPeriodMonth(employeeId, year, month)
                .ifPresent(liquidacionRepository::delete);

        Liquidacion l = new Liquidacion();
        l.setEmployee(employee);
        l.setPeriodYear(year);
        l.setPeriodMonth(month);

        // ---------- Días ----------
        int diasMes = period.lengthOfMonth();
        l.setDiasMes(diasMes);

        // Días hábiles (lunes a viernes) del período: base del prorrateo del sueldo.
        int diasHabiles = 0;
        for (int day = 1; day <= diasMes; day++) {
            java.time.DayOfWeek dow = start.plusDays(day - 1L).getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) diasHabiles++;
        }
        l.setDiasHabiles(diasHabiles);

        // Recuento de asistencia del período.
        List<Attendance> atts = attendanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end);
        int trabajados = 0, inasistencias = 0, licencias = 0;
        for (Attendance a : atts) {
            if (a.isComplete() && !a.isAbsence() && !a.isMedicalLicense()) trabajados++;
            if (a.isAbsence()) inasistencias++;
            if (a.isMedicalLicense()) licencias++;
        }
        l.setDiasTrabajados(trabajados);
        l.setDiasInasistencia(inasistencias);
        l.setDiasLicencia(licencias);

        // Valor diario y por hora. El sueldo cubre los días hábiles del período,
        // así que el valor diario se calcula contra los días hábiles.
        BigDecimal valorDia = employee.getBaseSalary()
                .divide(BigDecimal.valueOf(diasHabiles), 4, RoundingMode.HALF_UP);
        BigDecimal horasJornada = horasJornadaDelEmpleado(employee, start);
        BigDecimal valorHora = valorDia.divide(horasJornada, 4, RoundingMode.HALF_UP);

        // ---------- Horas extra (registro manual del administrador) ----------
        // Se suman las horas extra registradas por fecha dentro del período
        // (botón "Horas extra" de la lista de empleados) más las horas extra
        // opcionales que se puedan enviar en la solicitud.
        BigDecimal horasExtraRegistradas = horaExtraRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end)
                .stream().map(HoraExtra::getHoras).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal horasExtra = (horasExtras != null ? horasExtras : BigDecimal.ZERO)
                .add(horasExtraRegistradas);
        if (horasExtra.signum() < 0) {
            throw new BusinessException("Las horas extra no pueden ser negativas.");
        }
        BigDecimal horasExtraMult = param(SystemParam.HORAS_EXTRA_MULT, "1.5");
        BigDecimal montoHorasExtra = valorHora.multiply(horasExtra).multiply(horasExtraMult)
                .setScale(0, RoundingMode.HALF_UP);
        l.setHorasExtras(horasExtra);
        l.setMontoHorasExtras(montoHorasExtra);

        // ---------- Haberes ----------
        // Sueldo base completo; las inasistencias se descuentan por separado
        // como "Descuento por inasistencia".
        l.setSueldoBase(employee.getBaseSalary());

        // Gratificación legal (15% del sueldo, proporcional a días trabajados).
        BigDecimal gratificacionPct = param(SystemParam.GRATIFICACION_PCT, "0.15");
        BigDecimal gratificacion = employee.getBaseSalary()
                .multiply(gratificacionPct)
                .multiply(BigDecimal.valueOf(trabajados))
                .divide(BigDecimal.valueOf(diasHabiles), 0, RoundingMode.HALF_UP);
        l.setGratificacion(gratificacion);

// Asignación familiar por cargas, según el tramo de ingreso del trabajador
        // (valores vigentes desde el 01/07/2026, Ley 21.830).
        if (employee.getDependents() > 0) {
            BigDecimal asignacionPorCarga = asignacionFamiliarPorTramo(employee.getBaseSalary());
            l.setAsignacionFamiliar(asignacionPorCarga.multiply(BigDecimal.valueOf(employee.getDependents())));
        }

        // Bono de movilización (obligatorio).
        BigDecimal bonoMovilizacion = param(SystemParam.BONO_MOVILIZACION, "30000");
        l.setBonoMovilizacion(bonoMovilizacion);

        // Subsidio por licencia médica: días a cargo del empleador (hasta 3 por licencia).
        // Los días de licencia que el empleador NO paga se descuentan del sueldo.
        BigDecimal subsidioLicencia = BigDecimal.ZERO;
        int descuentoLicenciaDias = 0;
        List<LicenciaMedica> licenciasPeriodo = licenciaRepository.findByEmployeeId(employeeId).stream()
                .filter(lm -> overlaps(lm.getStartDate(), lm.getEndDate(), start, end))
                .toList();
        for (LicenciaMedica lm : licenciasPeriodo) {
            int overlap = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    max(lm.getStartDate(), start), min(lm.getEndDate(), end)) + 1;
            int empleadorDias = 0;
            if (lm.isEmployerPays()) {
                empleadorDias = (int) Math.min(overlap, DIA_LICENCIA_EMPLEADOR.intValue());
                subsidioLicencia = subsidioLicencia.add(valorDia.multiply(BigDecimal.valueOf(empleadorDias)));
            }
            // Días de licencia que quedan a cargo del trabajador (no pagados por el empleador).
            descuentoLicenciaDias += (overlap - empleadorDias);
        }
        l.setSubsidioLicenciaEmpleador(subsidioLicencia);

        BigDecimal descuentoLicencia = valorDia.multiply(BigDecimal.valueOf(descuentoLicenciaDias))
                .setScale(0, RoundingMode.HALF_UP);
        l.setDescuentoLicenciaDias(descuentoLicenciaDias);
        l.setDescuentoLicencia(descuentoLicencia);

        // ---------- Descuentos ----------
        // Base imponible para previsionales (AFP, SIS, salud, cesantía) e impuesto:
        // sólo las remuneraciones IMPONIBLES. La asignación familiar y el bono de
        // movilización NO son imponibles según la legislación chilena, por lo que
        // se excluyen de la base.
        BigDecimal baseImponible = l.getSueldoBase().add(gratificacion)
                .add(l.getMontoHorasExtras()).add(l.getOtrosHaberes());

        // AFP + SIS (previsional).
        BigDecimal afp = baseImponible.multiply(AFP_PCT).setScale(0, RoundingMode.HALF_UP);
        BigDecimal sisPct = param(SystemParam.SIS_PCT, "0.0144");
        BigDecimal sis = baseImponible.multiply(sisPct).setScale(0, RoundingMode.HALF_UP);
        l.setAfp(afp);
        l.setSis(sis);

        // Salud (7%).
        BigDecimal salud = baseImponible.multiply(SALUD_PCT).setScale(0, RoundingMode.HALF_UP);
        l.setSalud(salud);

        // Cesantía (parte del trabajador).
        BigDecimal cesPct = param(SystemParam.CESANTIA_EMPLEADO_PCT, "0.006");
        BigDecimal cesantia = baseImponible.multiply(cesPct).setScale(0, RoundingMode.HALF_UP);
        l.setCesantia(cesantia);

        // Descuento por horas extras (10% del monto horas extra).
        BigDecimal descuentoHorasExtras = l.getMontoHorasExtras().multiply(DESCUENTO_HORAS_EXTRAS_PCT).setScale(0, RoundingMode.HALF_UP);
        l.setDescuentoHorasExtras(descuentoHorasExtras);

        // Descuento por inasistencia (días hábiles no trabajados).
        BigDecimal descuentoFaltas = valorDia.multiply(BigDecimal.valueOf(inasistencias))
                .setScale(0, RoundingMode.HALF_UP);
        l.setDescuentoFaltas(descuentoFaltas);

        // Descuento por permisos laborales (descuento directo o saldo pendiente de devolución).
        BigDecimal descuentoPermisos = calcularDescuentoPermisos(employeeId, start, end, valorDia, valorHora);
        l.setOtrosDescuentos(descuentoPermisos);

        // Anticipos del período.
        BigDecimal anticipos = advanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end)
                .stream().map(Advance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        l.setAnticipos(anticipos);

        // Impuesto Único de 2ª categoría (sobre base gravada = imponible - previsional - salud).
        BigDecimal baseGravada = baseImponible.subtract(afp).subtract(salud)
                .max(BigDecimal.ZERO);
        BigDecimal impuestoUnico = calcularImpuestoUnico(baseGravada);
        l.setImpuestoUnico(impuestoUnico);

        // ---------- Totales ----------
        l.setTotalHaberes(l.getSueldoBase()
                .add(l.getGratificacion())
                .add(l.getAsignacionFamiliar())
                .add(l.getBonoMovilizacion())
                .add(l.getSubsidioLicenciaEmpleador())
                .add(l.getMontoHorasExtras())
                .add(l.getOtrosHaberes()));
        l.setTotalDescuentos(afp.add(sis).add(salud).add(impuestoUnico).add(cesantia)
                .add(descuentoFaltas).add(descuentoLicencia).add(anticipos).add(descuentoPermisos)
                .add(l.getDescuentoHorasExtras()));
        l.setLiquidoAPagar(l.getTotalHaberes().subtract(l.getTotalDescuentos()));

        // ---------- Detalle (líneas transparentes de la liquidación) ----------
        l.getDetalles().clear();
        addDetalle(l, "Sueldo base", true, l.getSueldoBase());
        if (gratificacion.signum() > 0) addDetalle(l, "Gratificación legal", true, gratificacion);
        if (l.getAsignacionFamiliar().signum() > 0)
            addDetalle(l, "Asignación familiar (" + employee.getDependents() + " cargas)", true, l.getAsignacionFamiliar());
        // Bono de movilización (obligatorio).
        addDetalle(l, "Bono de movilización", true, l.getBonoMovilizacion());
        if (l.getSubsidioLicenciaEmpleador().signum() > 0)
            addDetalle(l, "Subsidio licencia médica (días empleador)", true, l.getSubsidioLicenciaEmpleador());
        if (montoHorasExtra.signum() > 0)
            addDetalle(l, "Horas extra (" + horasExtra.stripTrailingZeros().toPlainString() + " h)", true, montoHorasExtra);
        addDetalle(l, "AFP (" + employee.getAfpFund() + " 10%)", false, afp);
        addDetalle(l, "SIS (seguro invalidez)", false, sis);
        addDetalle(l, "Salud (7%)", false, salud);
        addDetalle(l, "Cesantía (trabajador)", false, cesantia);
        addDetalle(l, "Impuesto Único 2ª categoría", false, impuestoUnico);
        if (l.getDescuentoHorasExtras().signum() > 0)
            addDetalle(l, "Descuento por horas extra (10%)", false, l.getDescuentoHorasExtras());
        if (descuentoFaltas.signum() > 0) addDetalle(l, "Descuento por inasistencia (" + inasistencias + " días)", false, descuentoFaltas);
        if (descuentoLicencia.signum() > 0) addDetalle(l, "Descuento por licencia médica (" + descuentoLicenciaDias + " días no pagados)", false, descuentoLicencia);
        if (anticipos.signum() > 0) addDetalle(l, "Anticipo de sueldo", false, anticipos);
        if (descuentoPermisos.signum() > 0) addDetalle(l, "Descuento por permisos laborales", false, descuentoPermisos);

        return mapper.toLiquidacionResponse(liquidacionRepository.save(l));
    }

    /** Genera las liquidaciones de todos los empleados de un período. */
    @Override
    @Transactional
    public int liquidarTodos(int year, int month) {
        List<Employee> employees = employeeRepository.findAll();
        int generadas = 0;
        for (Employee e : employees) {
            createLiquidacion(e.getId(), year, month);
            generadas++;
        }
        return generadas;
    }

    /** Lista las liquidaciones de un empleado. */
    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionResponse> listLiquidaciones(Long employeeId) {
        return liquidacionRepository.findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(employeeId)
                .stream().map(mapper::toLiquidacionResponse).toList();
    }

    /** Obtiene una liquidación por id. */
    @Override
    @Transactional(readOnly = true)
    public LiquidacionResponse getLiquidacion(Long liquidacionId) {
        Liquidacion l = liquidacionRepository.findById(liquidacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con id " + liquidacionId));
        return mapper.toLiquidacionResponse(l);
    }

    /** Genera el PDF (bytes) de una liquidación por id. */
    @Override
    @Transactional(readOnly = true)
    public byte[] generateLiquidacionPdf(Long liquidacionId) {
        Liquidacion l = liquidacionRepository.findById(liquidacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con id " + liquidacionId));
        return pdfService.generatePdf(l);
    }

    /** Marca una liquidación como pagada. */
    @Override
    @Transactional
    public StatusResponse marcarPagada(Long liquidacionId) {
        Liquidacion l = liquidacionRepository.findById(liquidacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con id " + liquidacionId));
        l.setPagada(true);
        liquidacionRepository.save(l);
        return StatusResponse.ok("Liquidación marcada como pagada");
    }

    // ========================================================================
    // ANTICIPOS
    // ========================================================================

    @Override
    @Transactional
    public AdvanceResponse addAdvance(Long employeeId, AdvanceRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + employeeId));
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new BusinessException("El monto del anticipo debe ser mayor a 0");
        }

        // Validar el tope legal del 15%: la SUMA de anticipos del período no puede
        // superar el 15% del sueldo base del trabajador para ese mes.
        LocalDate fecha = request.date() != null ? request.date() : LocalDate.now();
        YearMonth periodo = YearMonth.from(fecha);
        BigDecimal sumaPeriodo = advanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(
                        employeeId, periodo.atDay(1), periodo.atEndOfMonth())
                .stream().map(Advance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tope = employee.getBaseSalary()
                .multiply(new BigDecimal("0.15")).setScale(0, RoundingMode.HALF_UP);
        if (sumaPeriodo.add(request.amount()).compareTo(tope) > 0) {
            throw new BusinessException(
                    "El total de anticipos del período supera el 15% del sueldo base (" + tope + " CLP)");
        }

        Advance a = new Advance();
        a.setEmployee(employee);
        mapper.applyAdvanceRequest(a, request);
        return mapper.toAdvanceResponse(advanceRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceResponse> listAdvances(Long employeeId) {
        return advanceRepository.findByEmployeeId(employeeId)
                .stream().map(mapper::toAdvanceResponse).toList();
    }

    @Override
    @Transactional
    public StatusResponse deleteAdvance(Long advanceId) {
        Advance a = advanceRepository.findById(advanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Anticipo no encontrado con id " + advanceId));
        advanceRepository.delete(a);
        return StatusResponse.ok("Anticipo eliminado");
    }

    // ========================================================================
    // LICENCIAS MÉDICAS
    // ========================================================================

    @Override
    @Transactional
    public LicenciaResponse addLicencia(Long employeeId, LicenciaRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + employeeId));
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("La fecha de término debe ser posterior a la de inicio");
        }
        LicenciaMedica lic = new LicenciaMedica();
        lic.setEmployee(employee);
        mapper.applyLicenciaRequest(lic, request);
        return mapper.toLicenciaResponse(licenciaRepository.save(lic));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LicenciaResponse> listLicencias(Long employeeId) {
        return licenciaRepository.findByEmployeeId(employeeId)
                .stream().map(mapper::toLicenciaResponse).toList();
    }

    @Override
    @Transactional
    public StatusResponse deleteLicencia(Long licenciaId) {
        LicenciaMedica lic = licenciaRepository.findById(licenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Licencia no encontrada con id " + licenciaId));
        licenciaRepository.delete(lic);
        return StatusResponse.ok("Licencia médica eliminada");
    }

    // ========================================================================
    // HORAS EXTRA (registro manual por fecha)
    // ========================================================================

    @Override
    @Transactional
    public HoraExtraResponse addHoraExtra(Long employeeId, HoraExtraRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + employeeId));
        if (!employee.isEnabled()) {
            throw new BusinessException("El empleado está deshabilitado y no se pueden registrar horas extra.");
        }
        if (request.horas() == null || request.horas().signum() <= 0) {
            throw new BusinessException("Las horas extra deben ser un valor mayor a 0");
        }
        HoraExtra he = new HoraExtra();
        he.setEmployee(employee);
        mapper.applyHoraExtraRequest(he, request);
        return mapper.toHoraExtraResponse(horaExtraRepository.save(he));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoraExtraResponse> listHorasExtras(Long employeeId) {
        return horaExtraRepository.findByEmployeeIdOrderByDateDesc(employeeId)
                .stream().map(mapper::toHoraExtraResponse).toList();
    }

    @Override
    @Transactional
    public StatusResponse deleteHoraExtra(Long horaExtraId) {
        HoraExtra he = horaExtraRepository.findById(horaExtraId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de horas extra no encontrado con id " + horaExtraId));
        horaExtraRepository.delete(he);
        return StatusResponse.ok("Horas extra eliminadas");
    }

    // ========================================================================
    // MÉTODOS DE CÁLCULO AUXILIARES
    // ========================================================================

    /**
     * Calcula el descuento por permisos laborales aprobados del período.
     * Incluye tanto los permisos por DESCUENTO directo como los permisos por
     * DEVOLUCIÓN cuyo saldo aún no ha sido devuelto (quedan cobrados-descontados).
     */
    private BigDecimal calcularDescuentoPermisos(Long employeeId, LocalDate start, LocalDate end,
                                                 BigDecimal valorDia, BigDecimal valorHora) {
        List<PermisoLaboral> permisos = permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end);
        BigDecimal totalHoras = BigDecimal.ZERO;
        int totalDias = 0;

        for (PermisoLaboral p : permisos) {
            if (p.getEstado() != PermisoEstado.APROBADO) continue;

            if (p.getFormaCompensacion() == FormaCompensacion.DESCUENTO) {
                // Descuento directo: se cobra todo lo solicitado.
                if (p.getTipo() == PermisoTipo.POR_HORA) totalHoras = totalHoras.add(p.getHoras());
                else totalDias += p.getDias();
            } else {
                // Devolución: solo se descuenta el saldo NO devuelto aún.
                if (p.getTipo() == PermisoTipo.POR_HORA) {
                    totalHoras = totalHoras.add(p.getHoras().subtract(p.getHorasDevueltas()));
                } else {
                    totalDias += (p.getDias() - p.getDiasDevueltos());
                }
            }
        }

        return valorHora.multiply(totalHoras)
                .add(valorDia.multiply(BigDecimal.valueOf(totalDias)))
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el Impuesto Único de 2ª categoría mensual sobre la renta líquida
     * imponible (sueldo imponible menos cotizaciones previsionales y de salud),
     * según la tabla legal del Art. 43 N°1 LIR (Ley 21.210).
     *
     * Los tramos, tasas marginales y rebajas están fijados por ley en UTM
     * (13,5 / 30 / 50 / 70 / 90 / 120 / 310 UTM). Sólo cambia el valor en pesos
     * de la UTM de cada mes, por lo que la tabla se convierte con la UTM vigente.
     *
     * Fórmula: impuesto = (renta × factor del tramo) − (rebaja_UTM × UTM).
     */
    private BigDecimal calcularImpuestoUnico(BigDecimal baseGravada) {
        if (baseGravada.signum() <= 0) return BigDecimal.ZERO;
        BigDecimal utm = param(SystemParam.UTM, "71721");
        if (utm.signum() <= 0) return BigDecimal.ZERO;

        // Tramos legales (en UTM): {desdeUTM, hastaUTM, factor, rebajaUTM}
        BigDecimal nueve = new BigDecimal("13.5");
        BigDecimal[][] tabla = {
                { nueve,           new BigDecimal("30"),     new BigDecimal("0.04"),   new BigDecimal("0.54") },
                { new BigDecimal("30"),  new BigDecimal("50"),     new BigDecimal("0.08"),   new BigDecimal("1.74") },
                { new BigDecimal("50"),  new BigDecimal("70"),     new BigDecimal("0.135"),  new BigDecimal("4.49") },
                { new BigDecimal("70"),  new BigDecimal("90"),     new BigDecimal("0.23"),   new BigDecimal("11.14") },
                { new BigDecimal("90"),  new BigDecimal("120"),    new BigDecimal("0.304"),  new BigDecimal("17.80") },
                { new BigDecimal("120"), new BigDecimal("310"),    new BigDecimal("0.35"),   new BigDecimal("23.32") },
                { new BigDecimal("310"), new BigDecimal("100000"), new BigDecimal("0.40"),   new BigDecimal("38.82") }
        };

        BigDecimal enUTM = baseGravada.divide(utm, 4, RoundingMode.HALF_UP);
        if (enUTM.compareTo(nueve) <= 0) return BigDecimal.ZERO;

        for (BigDecimal[] tramo : tabla) {
            if (enUTM.compareTo(tramo[0]) > 0 && enUTM.compareTo(tramo[1]) <= 0) {
                BigDecimal rebaja = tramo[3].multiply(utm);
                BigDecimal impuesto = baseGravada.multiply(tramo[2])
                        .subtract(rebaja)
                        .max(BigDecimal.ZERO);
                return impuesto.setScale(0, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Valor de la asignación familiar por carga según el tramo de ingreso mensual
     * del trabajador. Montos vigentes desde el 01/07/2026 (Ley 21.830):
     *  - ingreso ≤ $649.039            → $22.601 por carga
     *  - $649.039 < ingreso ≤ $947.990  → $13.870 por carga
     *  - $947.990 < ingreso ≤ $1.478.539→ $4.382 por carga
     *  - ingreso > $1.478.539           → $0 (sin derecho)
     */
    private BigDecimal asignacionFamiliarPorTramo(BigDecimal ingreso) {
        if (ingreso.compareTo(new BigDecimal("649039")) <= 0) return new BigDecimal("22601");
        if (ingreso.compareTo(new BigDecimal("947990")) <= 0) return new BigDecimal("13870");
        if (ingreso.compareTo(new BigDecimal("1478539")) <= 0) return new BigDecimal("4382");
        return BigDecimal.ZERO;
    }

    /** Obtiene un parámetro del sistema o usa un valor por defecto. */
    private BigDecimal param(String key, String defaultValue) {
        return paramRepository.findById(key)
                .map(SystemParam::getValue)
                .orElseGet(() -> new BigDecimal(defaultValue));
    }

    /** Añade un detalle (línea) a la liquidación. */
    private void addDetalle(Liquidacion l, String concepto, boolean haber, BigDecimal monto) {
        if (monto == null) return;
        LiquidacionDetalle d = new LiquidacionDetalle();
        d.setLiquidacion(l);
        d.setConcepto(concepto);
        d.setHaber(haber);
        d.setMonto(monto);
        l.getDetalles().add(d);
    }

    private boolean overlaps(LocalDate a1, LocalDate a2, LocalDate b1, LocalDate b2) {
        return !a1.isAfter(b2) && !b1.isAfter(a2);
    }

    private LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    /**
     * Horas de la jornada diaria del empleado, tomadas del horario semanal que
     * rige en la fecha de referencia. Si no hay horario definido se asume 8h.
     */
    private BigDecimal horasJornadaDelEmpleado(Employee employee, LocalDate refDate) {
        WeeklySchedule ws = weeklyScheduleRepository
                .findByEmployeeAndStartDate(employee, ScheduleUtils.mondayOf(refDate))
                .orElse(null);
        return BigDecimal.valueOf(ScheduleUtils.hoursPerDay(ws, 8));
    }
}
