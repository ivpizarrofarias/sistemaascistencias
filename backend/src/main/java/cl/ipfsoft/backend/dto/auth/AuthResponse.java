package cl.ipfsoft.backend.dto.auth;

import cl.ipfsoft.backend.entity.Rol;

public record AuthResponse(
        String token,
        String username,
        Rol role,
        Long employeeId,
        String fullName
) {
}
