package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByRut(String rut);

    boolean existsByRut(String rut);

    /** Busca un empleado por su código de marcaje. */
    Optional<Empleado> findByCode(String code);

    /** Indica si el código de marcaje ya está en uso (para generar uno único). */
    boolean existsByCode(String code);

    List<Empleado> findByEnabledTrue();

    List<Empleado> findByEnabled(boolean enabled);
}
