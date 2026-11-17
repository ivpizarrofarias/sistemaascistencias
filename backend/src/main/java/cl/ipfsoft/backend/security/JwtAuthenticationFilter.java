package cl.ipfsoft.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta cada petición HTTP, extrae el token JWT del header
 * Authorization y, si es válido, autentica al usuario en el contexto de Spring Security.
 * De esta forma, los controladores pueden acceder a la identidad del usuario logueado
 * y a su rol (ADMIN o EMPLOYEE) mediante los mecanismos de autorización de Spring.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Servicio que genera y valida los tokens JWT.
    private final JwtService jwtService;

    // Servicio que carga los datos del usuario (username, password, roles) desde la BD.
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Método principal del filtro: se ejecuta para cada petición HTTP.
     * Si no hay header "Authorization: Bearer <token>", deja pasar la petición sin autenticar
     * (el SecurityConfig decidirá si el endpoint es público o requiere autenticación).
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Leer el header Authorization de la petición.
        String header = request.getHeader("Authorization");

        // 2. Si no hay header o no empieza con "Bearer ", no hay token que validar.
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitando el prefijo "Bearer ").
        String token = header.substring(7);

        try {
            // 4. Extraer el nombre de usuario contenido en el token.
            String username = jwtService.extractUsername(token);

            // 5. Si hay usuario y aún no hay una autenticación activa, validar el token.
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cargar el usuario real desde la base de datos.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Si el token es válido para ese usuario, construir la autenticación.
                if (jwtService.isTokenValid(token, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Establecer la autenticación en el contexto de seguridad.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Token inválido, expirado o malformado: no autenticar y continuar.
            // SecurityConfig decidirá si el endpoint requiere autenticación (401).
            SecurityContextHolder.clearContext();
        }

        // 6. Continuar con el resto de la cadena de filtros.
        filterChain.doFilter(request, response);
    }
}
