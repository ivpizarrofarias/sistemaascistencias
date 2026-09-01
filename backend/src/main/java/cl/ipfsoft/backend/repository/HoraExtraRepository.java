package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.HoraExtra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HoraExtraRepository extends JpaRepository<HoraExtra, Long> {

    List<HoraExtra> findByEmployeeIdOrderByDateDesc(Long employeeId);

    List<HoraExtra> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate start, LocalDate end);
}