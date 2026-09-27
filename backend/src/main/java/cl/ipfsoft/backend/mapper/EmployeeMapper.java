package cl.ipfsoft.backend.mapper;

import cl.ipfsoft.backend.dto.employee.EmployeeRequest;
import cl.ipfsoft.backend.dto.employee.EmployeeResponse;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.util.RutUtils;
import org.springframework.stereotype.Component;

/**
 * Conversión entre la entidad Empleado y sus DTOs.
 */
@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Empleado e) {
        return new EmployeeResponse(
                e.getId(),
                RutUtils.format(e.getRut()),
                e.getFirstName(),
                e.getLastName(),
                e.getFullName(),
                e.getPhone(),
                e.getEmail(),
                e.getHireDate(),
                e.getBaseSalary(),
                e.getDependents(),
                e.getPensionSystem(),
                e.getAfpFund(),
                e.getHealthSystem(),
                e.isEnabled(),
                e.getCode()
        );
    }

    /** Aplica los campos del request a la entidad (para create y update). */
    public void applyRequest(Empleado e, EmployeeRequest r) {
        e.setFirstName(r.firstName());
        e.setLastName(r.lastName());
        e.setPhone(r.phone());
        e.setEmail(r.email());
        e.setHireDate(r.hireDate());
        e.setBaseSalary(r.baseSalary());
        e.setDependents(r.dependents());
        e.setPensionSystem(r.pensionSystem() != null ? r.pensionSystem() : e.getPensionSystem());
        e.setAfpFund(r.afpFund() != null ? r.afpFund() : e.getAfpFund());
        e.setHealthSystem(r.healthSystem() != null ? r.healthSystem() : e.getHealthSystem());
    }
}
