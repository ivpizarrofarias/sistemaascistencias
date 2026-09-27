package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemParamRepository extends JpaRepository<ParametroSistema, String> {
}
