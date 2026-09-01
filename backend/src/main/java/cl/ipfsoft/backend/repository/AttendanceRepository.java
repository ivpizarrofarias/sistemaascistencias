package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Attendance;
import cl.ipfsoft.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate start, LocalDate end);

    List<Attendance> findByEmployeeIdOrderByDateAsc(Long employeeId);

    List<Attendance> findByEmployeeIdAndCompleteFalseOrderByDateDesc(Long employeeId);

    /** Último registro (de mayor fecha) del empleado, para validar secuencia. */
    Optional<Attendance> findTopByEmployeeOrderByDateDesc(Employee employee);

    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
}
