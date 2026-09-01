package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Advance;
import cl.ipfsoft.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    List<Advance> findByEmployeeId(Long employeeId);

    List<Advance> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate start, LocalDate end);
}
