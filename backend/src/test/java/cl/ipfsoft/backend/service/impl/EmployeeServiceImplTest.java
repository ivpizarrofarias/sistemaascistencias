package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.employee.EmployeeRequest;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.exception.BusinessException;
import cl.ipfsoft.backend.mapper.EmployeeMapper;
import cl.ipfsoft.backend.repository.AdvanceRepository;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.repository.AttendanceRepository;
import cl.ipfsoft.backend.repository.EmployeeRepository;
import cl.ipfsoft.backend.repository.LicenciaMedicaRepository;
import cl.ipfsoft.backend.repository.LiquidacionRepository;
import cl.ipfsoft.backend.repository.PermisoLaboralRepository;
import cl.ipfsoft.backend.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de empleados (con repositorios simulados con Mockito).
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock WeeklyScheduleRepository weeklyScheduleRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock PermisoLaboralRepository permisoLaboralRepository;
    @Mock LicenciaMedicaRepository licenciaMedicaRepository;
    @Mock AdvanceRepository advanceRepository;
    @Mock LiquidacionRepository liquidacionRepository;

    EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(employeeRepository, weeklyScheduleRepository, appUserRepository,
                attendanceRepository, permisoLaboralRepository, licenciaMedicaRepository,
                advanceRepository, liquidacionRepository, new EmployeeMapper());
    }

    private EmployeeRequest validRequest() {
        return new EmployeeRequest(
                "11111111-1", "Juan", "Perez", "123", "j@mail.cl",
                LocalDate.of(2020, 1, 1), new BigDecimal("1000000"),
                0, "AFP", "APROVISIONA", "ISAPRE");
    }

    @Test
    void create_conRutInvalido_lanzaExcepcion() {
        // RUT con dígito verificador incorrecto.
        EmployeeRequest bad = new EmployeeRequest(
                "11111111-9", "Juan", "Perez", null, null,
                LocalDate.of(2020, 1, 1), new BigDecimal("1000000"),
                0, "AFP", "A", "I");
        assertThrows(BusinessException.class, () -> service.create(bad));
    }

    @Test
    void create_conRutRepetido_lanzaExcepcion() {
        when(employeeRepository.existsByRut("11111111-1")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(validRequest()));
    }

    @Test
    void create_empleadoValido_normalizaYGeneraCodigo() {
        // Simular que no existe el RUT y que el código generado no colisiona.
        when(employeeRepository.existsByRut("11111111-1")).thenReturn(false);
        when(employeeRepository.existsByCode(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.create(validRequest());

        // El RUT debe quedar normalizado (con guion y DV) y formateado con puntos de miles.
        assertEquals("11.111.111-1", resp.rut());
        // El empleado recibe automáticamente un código de marcaje de 4 dígitos.
        assertNotNull(resp.code());
        assertTrue(resp.code().matches("\\d{4}"), "El código debe tener 4 dígitos, fue " + resp.code());
    }

    @Test
    @DisplayName("El código de marcaje es 4 dígitos, único e inmutable")
    void update_noModificaElCodigoDeMarcaje() {
        Empleado existente = new Empleado();
        existente.setId(7L);
        existente.setRut("11111111-1");
        existente.setCode("4832");
        existente.setFirstName("Juan");

        when(employeeRepository.findById(7L)).thenReturn(java.util.Optional.of(existente));
        when(employeeRepository.findByRut("11111111-1")).thenReturn(java.util.Optional.of(existente));
        when(employeeRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        // Se editan todos los datos editables, incluido el RUT y el nombre.
        var resp = service.update(7L, validRequest());

        assertEquals("4832", resp.code(),
                "El código de marcaje es la credencial del trabajador: editar no debe alterarlo");
    }

    @Test
    @DisplayName("El código se genera distinto para cada empleado nuevo")
    void create_codigosDistintosParaCadaEmpleado() {
        when(employeeRepository.existsByRut(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        java.util.Set<String> codigos = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) {
            codigos.add(service.create(validRequest()).code());
        }

        assertEquals(50, codigos.size(), "Los códigos generados deben ser distintos entre sí");
    }

    @Test
    void setEnabled_false_deshabilita() {
        Empleado e = new Empleado();
        e.setId(1L);
        e.setRut("11111111-1");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));

        service.setEnabled(1L, false);

        assertFalse(e.isEnabled());
    }

    @Test
    void setEnabled_true_habilita() {
        Empleado e = new Empleado();
        e.setId(1L);
        e.setRut("11111111-1");
        e.setEnabled(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));

        service.setEnabled(1L, true);

        assertTrue(e.isEnabled());
    }

    @Test
    void findById_empleadoInexistente_lanzaExcepcion() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.findById(99L));
    }
}