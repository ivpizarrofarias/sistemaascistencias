package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.permiso.DevolucionRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralResponse;
import cl.ipfsoft.backend.dto.permiso.ValidacionRequest;
import cl.ipfsoft.backend.entity.Employee;
import cl.ipfsoft.backend.entity.FormaCompensacion;
import cl.ipfsoft.backend.entity.PermisoEstado;
import cl.ipfsoft.backend.entity.PermisoLaboral;
import cl.ipfsoft.backend.entity.PermisoTipo;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.exception.ResourceNotFoundException;
import cl.ipfsoft.backend.mapper.PermisoMapper;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.PermisoLaboralRepository;
import cl.ipfsoft.backend.service.PermisoLaboralService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de permisos laborales.
 */
@Service
public class PermisoLaboralServiceImpl implements PermisoLaboralService {

    private final PermisoLaboralRepository permisoRepository;
    private final EmployeeRepository employeeRepository;
    private final PermisoMapper mapper;

    public PermisoLaboralServiceImpl(PermisoLaboralRepository permisoRepository,
                                     EmployeeRepository employeeRepository,
                                     PermisoMapper mapper) {
        this.permisoRepository = permisoRepository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    /**
     * Solicita un nuevo permiso. Todo nuevo permiso queda PENDIENTE de aprobación del admin.
     */
    @Override
    @Transactional
    public PermisoLaboralResponse request(Long employeeId, PermisoLaboralRequest request) {
        PermisoLaboral permiso = buildPermiso(employeeId, request);
        permiso.setEstado(PermisoEstado.PENDIENTE);
        permiso.setFechaSolicitud(LocalDate.now());
        permiso = permisoRepository.save(permiso);
        return mapper.toResponse(permiso);
    }

    /**
     * El administrador crea el permiso a nombre de un empleado y lo deja APROBADO
     * de inmediato (ya define la compensación: devolver o descontar).
     */
    @Override
    @Transactional
    public PermisoLaboralResponse createAsAdmin(Long employeeId, PermisoLaboralRequest request) {
        PermisoLaboral permiso = buildPermiso(employeeId, request);
        permiso.setEstado(PermisoEstado.APROBADO);
        permiso.setFechaSolicitud(LocalDate.now());
        permiso.setFechaValidacion(LocalDate.now());
        permiso.setValidadoPor("administrador");
        permiso = permisoRepository.save(permiso);
        return mapper.toResponse(permiso);
    }

    /**
     * Valida el empleado y la coherencia de la solicitud, y arma el permiso sin estado.
     */
    private PermisoLaboral buildPermiso(Long employeeId, PermisoLaboralRequest request) {
        // 1. Validar empleado y que esté habilitado.
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id " + employeeId));
        if (!employee.isEnabled()) {
            throw new BusinessException("El empleado está deshabilitado y no puede solicitar permisos.");
        }

        // 2. Validar coherencia según el tipo de permiso.
        if (request.tipo() == PermisoTipo.POR_HORA && (request.horas() == null || request.horas().signum() <= 0)) {
            throw new BusinessException("Debe indicar la cantidad de horas del permiso.");
        }
        if (request.tipo() == PermisoTipo.POR_DIA && (request.dias() == null || request.dias() <= 0)) {
            throw new BusinessException("Debe indicar la cantidad de días del permiso.");
        }

        // 3. Construir la entidad.
        PermisoLaboral permiso = new PermisoLaboral();
        permiso.setEmployee(employee);
        mapper.applyRequest(permiso, request);
        return permiso;
    }

    /** Lista los permisos de un empleado en el rango indicado. */
    @Override
    @Transactional(readOnly = true)
    public List<PermisoLaboralResponse> listByEmployee(Long employeeId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.of(1900, 1, 1);
        LocalDate end = to != null ? to : LocalDate.now().plusYears(10);
        return permisoRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end)
                .stream().map(mapper::toResponse).toList();
    }

    /** Lista todos los permisos (para el administrador). */
    @Override
    @Transactional(readOnly = true)
    public List<PermisoLaboralResponse> findAll() {
        return permisoRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    /** El administrador aprueba o rechaza el permiso. */
    @Override
    @Transactional
    public PermisoLaboralResponse validate(Long permisoId, ValidacionRequest request) {
        PermisoLaboral permiso = getPermiso(permisoId);

        // No se puede revalidar lo ya validado (excepto cambiar de decisión es libre, se respeta).
        if (request.estado() == PermisoEstado.PENDIENTE) {
            throw new BusinessException("Solo puede aprobar o rechazar, no dejar pendiente.");
        }

        permiso.setEstado(request.estado());
        permiso.setFechaValidacion(LocalDate.now());
        permiso.setValidadoPor(request.observacion() != null ? request.observacion() : "administrador");
        permiso = permisoRepository.save(permiso);

        return mapper.toResponse(permiso);
    }

    /**
     * Registra la devolución (compensación) del tiempo pedido por permiso.
     * Solo válido para permisos APROBADOS con forma de compensación DEVOLVER.
     * Las devoluciones se acumulan; si el saldo queda en cero, el permiso queda compensado.
     */
    @Override
    @Transactional
    public PermisoLaboralResponse registerDevolucion(Long permisoId, DevolucionRequest request) {
        PermisoLaboral permiso = getPermiso(permisoId);

        // Validar el estado del permiso.
        if (permiso.getEstado() != PermisoEstado.APROBADO) {
            throw new BusinessException("Solo puede devolver tiempo en permisos aprobados.");
        }
        if (permiso.getFormaCompensacion() != FormaCompensacion.DEVOLVER) {
            throw new BusinessException("Este permiso no es por devolución; se queda como descuento.");
        }

        // Aplicar la devolución según el tipo (horas o días).
        if (permiso.getTipo() == PermisoTipo.POR_HORA) {
            if (request.horasDevueltas() == null || request.horasDevueltas().signum() <= 0) {
                throw new BusinessException("Debe indicar las horas devueltas.");
            }
            BigDecimal nuevas = permiso.getHorasDevueltas().add(request.horasDevueltas());
            if (nuevas.compareTo(permiso.getHoras()) > 0) {
                throw new BusinessException("No puede devolver más horas de las solicitadas ("
                        + permiso.getHoras() + ").");
            }
            permiso.setHorasDevueltas(nuevas);
        } else {
            if (request.diasDevueltos() == null || request.diasDevueltos() <= 0) {
                throw new BusinessException("Debe indicar los días devueltos.");
            }
            int nuevos = permiso.getDiasDevueltos() + request.diasDevueltos();
            if (nuevos > permiso.getDias()) {
                throw new BusinessException("No puede devolver más días de los solicitados ("
                        + permiso.getDias() + ").");
            }
            permiso.setDiasDevueltos(nuevos);
        }

        permiso = permisoRepository.save(permiso);
        return mapper.toResponse(permiso);
    }

    /** Elimina un permiso. */
    @Override
    @Transactional
    public StatusResponse delete(Long permisoId) {
        PermisoLaboral permiso = getPermiso(permisoId);
        permisoRepository.delete(permiso);
        return StatusResponse.ok("Permiso eliminado");
    }

    /** Obtiene un permiso por id o lanza excepción. */
    private PermisoLaboral getPermiso(Long id) {
        return permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id " + id));
    }
}
