package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.AppUser;
import cl.ipfsoft.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Elimina los usuarios de acceso asociados a un empleado (para borrado en cascada). */
    void deleteByEmployee(Employee employee);
}
