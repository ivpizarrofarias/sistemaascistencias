package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.SystemParam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemParamRepository extends JpaRepository<SystemParam, String> {
}
