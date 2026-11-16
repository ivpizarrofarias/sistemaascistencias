package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.user.SystemUserRequest;
import cl.ipfsoft.backend.dto.user.SystemUserResponse;
import cl.ipfsoft.backend.entity.Rol;
import cl.ipfsoft.backend.entity.Usuario;
import cl.ipfsoft.backend.exception.ResourceNotFoundException;
import cl.ipfsoft.backend.repository.AppUserRepository;
import cl.ipfsoft.backend.service.SystemUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemUserServiceImpl implements SystemUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemUserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemUserResponse> findAll() {
        return userRepository.findAll().stream()
                .filter(u -> u.getEmployee() == null) // solo usuarios del sistema (no vinculados a empleado)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemUserResponse findById(Long id) {
        return toResponse(getSystemUser(id));
    }

    @Override
    @Transactional
    public SystemUserResponse create(SystemUserRequest request) {
        validateRole(request.role());
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un usuario");
        }
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        Usuario user = new Usuario();
        user.setUsername(username);
        user.setFullName(trimToNull(request.fullName()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        user.setEmployee(null);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public SystemUserResponse update(Long id, SystemUserRequest request) {
        validateRole(request.role());
        Usuario user = getSystemUser(id);
        String username = request.username().trim();
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        user.setUsername(username);
        user.setFullName(trimToNull(request.fullName()));
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public StatusResponse setEnabled(Long id, boolean enabled) {
        Usuario user = getSystemUser(id);
        user.setEnabled(enabled);
        userRepository.save(user);
        return StatusResponse.ok(enabled ? "Usuario habilitado" : "Usuario deshabilitado");
    }

    @Override
    @Transactional
    public StatusResponse delete(Long id) {
        Usuario user = getSystemUser(id);
        userRepository.delete(user);
        return StatusResponse.ok("Usuario eliminado");
    }

    private Usuario getSystemUser(Long id) {
        Usuario user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (user.getEmployee() != null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return user;
    }

    private void validateRole(Rol role) {
        if (role != Rol.ADMIN && role != Rol.SUPERVISOR) {
            throw new IllegalArgumentException("Solo se pueden gestionar usuarios Administrador o Supervisor");
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private SystemUserResponse toResponse(Usuario u) {
        return new SystemUserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.isEnabled());
    }
}
