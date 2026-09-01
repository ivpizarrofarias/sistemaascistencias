package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.FormaCompensacion;
import cl.ipfsoft.backend.entity.PermisoEstado;
import cl.ipfsoft.backend.entity.PermisoLaboral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PermisoLaboralRepository extends JpaRepository<PermisoLaboral, Long> {

    List<PermisoLaboral> findByEmployeeIdOrderByDateDesc(Long employeeId);

    List<PermisoLaboral> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate from, LocalDate to);

    List<PermisoLaboral> findByEmployeeIdAndEstadoAndFormaCompensacion(
            Long employeeId, PermisoEstado estado, FormaCompensacion forma);
}
