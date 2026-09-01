package cl.ipfsoft.backend.dto.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Petición de alta/edición de empleado.
 * El empleado ya NO tiene usuario ni contraseña: el sistema le genera un
 * código de marcaje automáticamente (no se envía en la petición).
 * El horario de trabajo se gestiona aparte (vista de horarios), no aquí.
 */
public record EmployeeRequest(
        @NotBlank(message = "El RUT es obligatorio") String rut,
        @NotBlank(message = "El nombre es obligatorio") String firstName,
        @NotBlank(message = "El apellido es obligatorio") String lastName,
        String phone,
        String email,
        @NotNull(message = "La fecha de ingreso es obligatoria") LocalDate hireDate,
        @NotNull(message = "El sueldo base es obligatorio")
        @DecimalMin(value = "0.01", message = "El sueldo base debe ser mayor a 0") BigDecimal baseSalary,
        @Min(value = 0, message = "Las cargas no pueden ser negativas") int dependents,
        String pensionSystem,
        String afpFund,
        String healthSystem
) {
}
