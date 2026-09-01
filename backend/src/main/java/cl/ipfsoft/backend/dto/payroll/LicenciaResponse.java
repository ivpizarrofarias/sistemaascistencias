package cl.ipfsoft.backend.dto.payroll;

import java.time.LocalDate;

public record LicenciaResponse(
        Long id,
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        String motivo,
        boolean employerPays
) {
}
