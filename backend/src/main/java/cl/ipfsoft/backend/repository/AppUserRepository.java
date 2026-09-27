package cl.ipfsoft.backend.repository;

import cl.ipfsoft.backend.entity.Usuario;
import cl.ipfsoft.backend.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Elimina los usuarios de acceso asociados a un empleado (para borrado en cascada). */
    void deleteByEmployee(Empleado employee);
}
