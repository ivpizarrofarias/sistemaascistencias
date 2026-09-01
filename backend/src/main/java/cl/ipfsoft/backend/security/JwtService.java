package cl.ipfsoft.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = buildKey(secret);
        this.expirationMs = expirationMs;
    }

    private SecretKey buildKey(String secret) {
        // Primero se intenta decodificar como Base64 (formato recomendado en producción).
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            // Si no es Base64 válida, se usa la cadena directamente (debe medir al menos
            // 256 bits = 32 bytes para HS256).
            return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public String generateToken(String username, String role, Long employeeId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("employeeId", employeeId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, c -> c.get("role", String.class));
    }

    public Long extractEmployeeId(String token) {
        return extractClaim(token, c -> c.get("employeeId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
