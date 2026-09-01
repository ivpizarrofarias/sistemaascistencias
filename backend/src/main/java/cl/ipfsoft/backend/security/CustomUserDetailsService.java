package cl.ipfsoft.backend.security;

import cl.ipfsoft.backend.entity.AppUser;
import cl.ipfsoft.backend.entity.Role;
import cl.ipfsoft.backend.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        String authority = "ROLE_" + (user.getRole() == Role.ADMIN ? "ADMIN" : "EMPLOYEE");
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
