package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.employee.EmployeeRequest;
import cl.ipfsoft.backend.dto.employee.EmployeeResponse;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.exception.ResourceNotFoundException;
import cl.ipfsoft.backend.mapper.EmployeeMapper;
import cl.ipfsoft.backend.repository.AdvanceRepository;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.repository.AttendanceRepository;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.LicenciaMedicaRepository;
import cl.ipfsoft.backend.repository.LiquidacionRepository;
import cl.ipfsoft.backend.repository.PermisoLaboralRepository;
import cl.ipfsoft.backend.repository.WeeklyScheduleRepository;
import cl.ipfsoft.backend.service.EmployeeService;
import cl.ipfsoft.backend.util.CodeGenerator;
import cl.ipfsoft.backend.util.RutUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de empleados.
 *
 * El empleado NO tiene cuenta de acceso (ni usuario ni contraseña): al crearlo
 * el sistema genera automáticamente un código de marcaje único que el trabajador
 * usa para registrar su asistencia en el kiosco/marcador móvil.
 * Las operaciones de creación/modificación/deshabilitado están restringidas a
 * administradores (eso se controla a nivel de controlador con la seguridad).
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final AppUserRepository appUserRepository;
    private final AttendanceRepository attendanceRepository;
    private final PermisoLaboralRepository permisoLaboralRepository;
    private final LicenciaMedicaRepository licenciaMedicaRepository;
    private final AdvanceRepository advanceRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final EmployeeMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               WeeklyScheduleRepository weeklyScheduleRepository,
                               AppUserRepository appUserRepository,
                               AttendanceRepository attendanceRepository,
                               PermisoLaboralRepository permisoLaboralRepository,
                               LicenciaMedicaRepository licenciaMedicaRepository,
                               AdvanceRepository advanceRepository,
                               LiquidacionRepository liquidacionRepository,
                               EmployeeMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.appUserRepository = appUserRepository;
        this.attendanceRepository = attendanceRepository;
        this.permisoLaboralRepository = permisoLaboralRepository;
        this.licenciaMedicaRepository = licenciaMedicaRepository;
        this.advanceRepository = advanceRepository;
        this.liquidacionRepository = liquidacionRepository;
        this.mapper = mapper;
    }

    /** Devuelve todos los empleados. */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        // Se listan todos, tanto habilitados como deshabilitados, para la vista del admin.
        return employeeRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    /** Busca un empleado por id. */
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return mapper.toResponse(getEmployee(id));
    }

    /**
     * Crea un empleado nuevo.
     * Valida el RUT, comprueba que no esté repetido, genera su código de marcaje
     * único y guarda el empleado.
     */
    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        // 1. Normalizar y validar el RUT chileno (formato y dígito verificador).
        String normalizedRut = normalizeRut(request.rut());

        // 2. Verificar que el RUT no esté ya registrado.
        if (employeeRepository.existsByRut(normalizedRut)) {
            throw new BusinessException("Ya existe un empleado con el RUT " + normalizedRut);
        }

        // 3. Construir la entidad a partir del request.
        Empleado employee = new Empleado();
        employee.setRut(normalizedRut);
        mapper.applyRequest(employee, request);

        // 4. Generar el código de marcaje: 4 dígitos aleatorios y únicos. Es la
        //    credencial del trabajador en el kiosco, por eso no depende del RUT
        //    ni del nombre y se asigna una sola vez.
        employee.setCode(CodeGenerator.uniqueCode(employeeRepository::existsByCode));

        // 5. Guardar el empleado.
        Empleado saved = employeeRepository.save(employee);

        return mapper.toResponse(saved);
    }

    /**
     * Actualiza los datos de un empleado existente.
     * No se permite duplicar RUT (se valida salvo que sea el mismo empleado).
     * El código de marcaje NO se modifica al editar.
     */
    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Empleado employee = getEmployee(id);

        // 1. Normalizar RUT y permitir cambiarlo solo si no colisiona con otro empleado.
        String normalizedRut = normalizeRut(request.rut());
        employeeRepository.findByRut(normalizedRut).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Ya existe un empleado con el RUT " + normalizedRut);
            }
        });
        employee.setRut(normalizedRut);

        // 2. Aplicar el resto de los campos.
        mapper.applyRequest(employee, request);

        // 3. El código de marcaje es inmutable: se restituye explícitamente para
        //    que ningun camino futuro lo altere por accidente al editar.
        if (employee.getCode() == null) {
            // Solo posible para empleados dados de alta antes de que existiera el código.
            employee.setCode(CodeGenerator.uniqueCode(employeeRepository::existsByCode));
        }

        return mapper.toResponse(employeeRepository.save(employee));
    }

    /** Habilita o deshabilita a un empleado (solo administrador). */
    @Override
    @Transactional
    public StatusResponse setEnabled(Long id, boolean enabled) {
        Empleado employee = getEmployee(id);
        employee.setEnabled(enabled);
        employeeRepository.save(employee);
        return StatusResponse.ok(enabled
                ? "Empleado habilitado correctamente"
                : "Empleado deshabilitado. Ya no puede marcar asistencia.");
    }

    /** Elimina un empleado. */
    @Override
    @Transactional
    public StatusResponse delete(Long id) {
        Empleado employee = getEmployee(id);
        // Borrado en cascada: primero los registros que dependen del empleado.
        appUserRepository.deleteByEmployee(employee);
        attendanceRepository.deleteAll(attendanceRepository.findByEmployeeIdOrderByDateAsc(id));
        permisoLaboralRepository.deleteAll(permisoLaboralRepository.findByEmployeeIdOrderByDateDesc(id));
        licenciaMedicaRepository.deleteAll(licenciaMedicaRepository.findByEmployeeId(id));
        advanceRepository.deleteAll(advanceRepository.findByEmployeeId(id));
        liquidacionRepository.deleteAll(liquidacionRepository.findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(id));
        weeklyScheduleRepository.deleteByEmployee(employee);
        employeeRepository.delete(employee);
        return StatusResponse.ok("Empleado eliminado");
    }

    /**
     * Normaliza y valida el RUT, convirtiendo los errores de formato en
     * BusinessException para que la API responda un error 400 de negocio
     * (en lugar de una IllegalArgumentException genérica 500).
     */
    private String normalizeRut(String rut) {
        try {
            return RutUtils.normalize(rut);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Obtiene un empleado por id o lanza excepción si no existe.
     */
    private Empleado getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + id));
    }
}