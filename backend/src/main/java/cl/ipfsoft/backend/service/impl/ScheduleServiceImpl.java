package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleRequest;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleResponse;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import cl.ipfsoft.backend.exception.ResourceNotFoundException;
import cl.ipfsoft.backend.mapper.ScheduleMapper;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.WeeklyScheduleRepository;
import cl.ipfsoft.backend.service.ScheduleService;
import cl.ipfsoft.backend.util.ScheduleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de horarios semanales.
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final WeeklyScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleMapper mapper;
    private final SchedulePdfService pdfService;

    public ScheduleServiceImpl(WeeklyScheduleRepository scheduleRepository,
                               EmployeeRepository employeeRepository,
                               ScheduleMapper mapper,
                               SchedulePdfService pdfService) {
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
        this.pdfService = pdfService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyScheduleResponse> listByEmployee(Long employeeId) {
        Empleado employee = getEmployee(employeeId);
        return scheduleRepository.findByEmployeeOrderByStartDateDesc(employee).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyScheduleResponse getByEmployeeAndWeek(Long employeeId, LocalDate startDate) {
        Empleado employee = getEmployee(employeeId);
        LocalDate monday = mondayOf(startDate);
        return scheduleRepository.findByEmployeeAndStartDate(employee, monday)
                .map(mapper::toResponse)
                .orElseGet(() -> mapper.toResponse(mapper.empty(employee, monday)));
    }

    @Override
    @Transactional
    public WeeklyScheduleResponse save(WeeklyScheduleRequest request) {
        Empleado employee = getEmployee(request.employeeId());
        LocalDate monday = mondayOf(request.startDate());

        // Upsert: si ya existe el horario de esa semana, se actualiza.
        HorarioSemanal ws = scheduleRepository
                .findByEmployeeAndStartDate(employee, monday)
                .orElseGet(HorarioSemanal::new);
        mapper.applyRequest(ws, new WeeklyScheduleRequest(employee.getId(), monday, request.label(), request.days()), employee);
        return mapper.toResponse(scheduleRepository.save(ws));
    }

    @Override
    @Transactional
    public WeeklyScheduleResponse update(Long id, WeeklyScheduleRequest request) {
        HorarioSemanal ws = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con id " + id));
        Empleado employee = getEmployee(request.employeeId());
        mapper.applyRequest(ws, new WeeklyScheduleRequest(employee.getId(), mondayOf(request.startDate()), request.label(), request.days()), employee);
        return mapper.toResponse(scheduleRepository.save(ws));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        HorarioSemanal ws = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con id " + id));
        scheduleRepository.delete(ws);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateSchedulePdf(Long employeeId, LocalDate startDate) {
        Empleado employee = getEmployee(employeeId);
        if (!employee.isEnabled()) {
            throw new cl.ipfsoft.backend.exception.BusinessException(
                    "El empleado está deshabilitado y no se puede generar su horario.");
        }
        LocalDate monday = mondayOf(startDate);
        HorarioSemanal ws = scheduleRepository.findByEmployeeAndStartDate(employee, monday)
                .orElseGet(() -> mapper.empty(employee, monday));
        return pdfService.generatePdf(ws);
    }

    /** Devuelve el lunes de la semana a la que pertenece una fecha. */
    public static LocalDate mondayOf(LocalDate date) {
        return ScheduleUtils.mondayOf(date);
    }

    private Empleado getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + id));
    }
}
