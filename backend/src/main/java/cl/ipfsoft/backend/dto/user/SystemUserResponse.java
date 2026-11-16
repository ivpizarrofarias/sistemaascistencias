package cl.ipfsoft.backend.dto.user;

import cl.ipfsoft.backend.entity.Rol;

public record SystemUserResponse(
        Long id,
        String username,
        String fullName,
        Rol role,
        boolean enabled
) {
}
