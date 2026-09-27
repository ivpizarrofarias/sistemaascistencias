package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Anticipo;
import cl.ipfsoft.backend.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdvanceRepository extends JpaRepository<Anticipo, Long> {

    List<Anticipo> findByEmployeeId(Long employeeId);

    List<Anticipo> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate start, LocalDate end);
}
