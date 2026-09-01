package cl.ipfsoft.backend.dto.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String rut,
        String firstName,
        String lastName,
        String fullName,
        String phone,
        String email,
        LocalDate hireDate,
        BigDecimal baseSalary,
        int dependents,
        String pensionSystem,
        String afpFund,
        String healthSystem,
        boolean enabled,
        String code
) {
}
