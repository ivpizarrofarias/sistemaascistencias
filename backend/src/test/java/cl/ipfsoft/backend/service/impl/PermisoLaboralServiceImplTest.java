package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.permiso.DevolucionRequest;
import cl.ipfsoft.backend.dto.permiso.PermisoLaboralRequest;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.FormaCompensacion;
import cl.ipfsoft.backend.entity.PermisoEstado;
import cl.ipfsoft.backend.entity.PermisoLaboral;
import cl.ipfsoft.backend.entity.PermisoTipo;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.mapper.PermisoMapper;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.PermisoLaboralRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de permisos laborales.
 */
@ExtendWith(MockitoExtension.class)
class PermisoLaboralServiceImplTest {

    @Mock PermisoLaboralRepository permisoRepository;
    @Mock EmployeeRepository employeeRepository;

    PermisoLaboralServiceImpl service;
    Empleado employee;

    @BeforeEach
    void setUp() {
        service = new PermisoLaboralServiceImpl(permisoRepository, employeeRepository, new PermisoMapper());
        employee = new Empleado();
        employee.setId(1L);
        employee.setFirstName("Juan");
        employee.setLastName("Perez");
        employee.setEnabled(true);
    }

    private PermisoLaboralRequest requestPorHora() {
        return new PermisoLaboralRequest(LocalDate.of(2026, 8, 10), PermisoTipo.POR_HORA,
                new BigDecimal("2"), null, "Trámite personal", FormaCompensacion.DESCUENTO);
    }

    @Test
    void request_permisoPorHoras_quedaPendiente() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(permisoRepository.save(any(PermisoLaboral.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.request(1L, requestPorHora());

        assertEquals(PermisoEstado.PENDIENTE, resp.estado());
        assertEquals(0, new BigDecimal("2").compareTo(resp.horas()));
        assertTrue(resp.saldoPendiente() == false);
    }

    @Test
    void request_porHorasSinCantidad_lanzaExcepcion() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        PermisoLaboralRequest bad = new PermisoLaboralRequest(LocalDate.of(2026, 8, 10),
                PermisoTipo.POR_HORA, null, null, "motivo", FormaCompensacion.DESCUENTO);
        assertThrows(BusinessException.class, () -> service.request(1L, bad));
    }

    @Test
    void request_empleadoDeshabilitado_lanzaExcepcion() {
        employee.setEnabled(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(BusinessException.class, () -> service.request(1L, requestPorHora()));
    }

    @Test
    void validate_apruebaPermiso() {
        PermisoLaboral permiso = new PermisoLaboral();
        permiso.setId(10L);
        permiso.setEmployee(employee);
        permiso.setTipo(PermisoTipo.POR_HORA);
        permiso.setHoras(new BigDecimal("2"));
        permiso.setFormaCompensacion(FormaCompensacion.DESCUENTO);
        permiso.setEstado(PermisoEstado.PENDIENTE);

        when(permisoRepository.findById(10L)).thenReturn(Optional.of(permiso));
        when(permisoRepository.save(any(PermisoLaboral.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.validate(10L, new cl.ipfsoft.backend.dto.permiso.ValidacionRequest(
                PermisoEstado.APROBADO, "aprobado"));
        assertEquals(PermisoEstado.APROBADO, resp.estado());
    }

    @Test
    void validate_estadoPendiente_rechazado() {
        PermisoLaboral permiso = new PermisoLaboral();
        permiso.setId(10L);
        permiso.setEmployee(employee);
        permiso.setEstado(PermisoEstado.PENDIENTE);
        when(permisoRepository.findById(10L)).thenReturn(Optional.of(permiso));

        assertThrows(BusinessException.class, () -> service.validate(
                10L, new cl.ipfsoft.backend.dto.permiso.ValidacionRequest(PermisoEstado.PENDIENTE, null)));
    }

    @Test
    void registerDevolucion_soloEnPermisosAprobadosDevolver() {
        PermisoLaboral permiso = new PermisoLaboral();
        permiso.setId(10L);
        permiso.setEmployee(employee);
        permiso.setTipo(PermisoTipo.POR_HORA);
        permiso.setHoras(new BigDecimal("4"));
        permiso.setHorasDevueltas(BigDecimal.ZERO);
        permiso.setEstado(PermisoEstado.APROBADO);
        permiso.setFormaCompensacion(FormaCompensacion.DEVOLVER);

        when(permisoRepository.findById(10L)).thenReturn(Optional.of(permiso));
        when(permisoRepository.save(any(PermisoLaboral.class))).thenAnswer(inv -> inv.getArgument(0));

        // Devuelve 2 de las 4 horas.
        var resp = service.registerDevolucion(10L, new DevolucionRequest(new BigDecimal("2"), null));
        assertEquals(0, new BigDecimal("2").compareTo(resp.horasDevueltas()));
        // Aún queda saldo pendiente.
        assertTrue(resp.saldoPendiente());
    }

    @Test
    void registerDevolucion_noPuedeDevolverMasDeLoPedido() {
        PermisoLaboral permiso = new PermisoLaboral();
        permiso.setId(10L);
        permiso.setEmployee(employee);
        permiso.setTipo(PermisoTipo.POR_HORA);
        permiso.setHoras(new BigDecimal("2"));
        permiso.setHorasDevueltas(BigDecimal.ZERO);
        permiso.setEstado(PermisoEstado.APROBADO);
        permiso.setFormaCompensacion(FormaCompensacion.DEVOLVER);

        when(permisoRepository.findById(10L)).thenReturn(Optional.of(permiso));

        assertThrows(BusinessException.class, () -> service.registerDevolucion(
                10L, new DevolucionRequest(new BigDecimal("5"), null)));
    }
}
