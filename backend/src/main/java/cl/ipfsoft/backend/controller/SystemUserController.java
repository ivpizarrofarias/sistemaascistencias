package cl.ipfsoft.backend.controller;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.user.SystemUserRequest;
import cl.ipfsoft.backend.dto.user.SystemUserResponse;
import cl.ipfsoft.backend.service.SystemUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestión de usuarios del sistema (ADMIN y SUPERVISOR). Solo accesible para ADMIN.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @GetMapping
    public List<SystemUserResponse> findAll() {
        return systemUserService.findAll();
    }

    @GetMapping("/{id}")
    public SystemUserResponse findById(@PathVariable Long id) {
        return systemUserService.findById(id);
    }

    @PostMapping
    public ResponseEntity<SystemUserResponse> create(@Valid @RequestBody SystemUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(systemUserService.create(request));
    }

    @PutMapping("/{id}")
    public SystemUserResponse update(@PathVariable Long id, @Valid @RequestBody SystemUserRequest request) {
        return systemUserService.update(id, request);
    }

    @PutMapping("/{id}/enabled")
    public StatusResponse setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        return systemUserService.setEnabled(id, enabled);
    }

    @DeleteMapping("/{id}")
    public StatusResponse delete(@PathVariable Long id) {
        return systemUserService.delete(id);
    }
}
