package cl.ipfsoft.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.util.List;

/**
 * Configuración de seguridad de la aplicación.
 *
 * Define:
 *  - Un codificador de contraseñas (BCrypt) para guardar y verificar claves de forma segura.
 *  - La cadena de filtros de seguridad (SecurityFilterChain) indicando qué endpoints son
 *    públicos, cuáles requieren autenticación y cuáles requieren rol de administrador.
 *  - El filtro JWT para autenticar peticiones mediante token.
 *  - El manejo de CORS para permitir peticiones desde el frontend (React/Vite).
 *  - Sesiones sin estado (stateless) porque usamos tokens JWT.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Codificador de contraseñas: se usa BCrypt (hash con "sal" para mayor seguridad). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Reloj del sistema. Se inyecta en los servicios que dependen de la fecha/hora actual,
     * lo que facilita las pruebas unitarias (se puede fijar una fecha determinada).
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {
        http
                // Deshabilitar CSRF: la API es stateless y se protege con JWT.
                .csrf(AbstractHttpConfigurer::disable)
                // No guardar sesiones en el servidor (stateless).
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Petición sin token a un endpoint protegido -> 401 (no 403).
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"status\":401,\"error\":\"No autenticado\",\"message\":\"Debe proporcionar un token valido\"}");
                        })
                )
                // Habilitar CORS con la configuración definida abajo.
                .cors(Customizer.withDefaults())
                // Definir las reglas de autorización por ruta.
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (login y marcaje self-service del kiosco).
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/attendance/mark").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/attendance/status").permitAll()
                        // No autenticados pero para ver el estado de salud de la app.
                        .requestMatchers("/", "/error").permitAll()
                        // El resto de peticiones requiren token válido.
                        .anyRequest().authenticated()
                )
                // Añadir el filtro JWT ANTES del filtro de autenticación por username/password,
                // para que el token se procese antes de decidir si la petición está autorizada.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración de CORS: se permite el frontend (React/Vite).
     * En desarrollo se permiten los orígenes de localhost en cualquier puerto
     * (por ejemplo http://localhost:5173, http://localhost:5174, etc.), porque
     * Vite puede elegir puertos alternos si el 5173 está ocupado.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Patrones de orígenes permitidos (localhost con cualquier puerto).
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
