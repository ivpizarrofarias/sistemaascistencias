package cl.ipfsoft.backend.dto.auth;

import cl.ipfsoft.backend.entity.Role;

public record AuthResponse(
        String token,
        String username,
        Role role,
        Long employeeId,
        String fullName
) {
}
