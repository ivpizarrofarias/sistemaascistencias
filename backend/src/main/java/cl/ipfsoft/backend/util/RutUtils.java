package cl.ipfsoft.backend.util;

import java.util.regex.Pattern;

/**
 * Utilidades para el tratamiento del RUT chileno.
 * Formato aceptado: 12345678-5 o 12345678k (guion opcional, mayúsculas/minúsculas).
 * La validación usa el módulo 11 (algoritmo oficial del SII).
 */
public final class RutUtils {

    private static final Pattern RUT_PATTERN = Pattern.compile(
            "^\\d{7,8}[-]?[0-9kK]$"
    );

    private RutUtils() {
    }

    /**
     * Valida el formato y el dígito verificador de un RUT.
     */
    public static boolean isValid(String rut) {
        if (rut == null || !RUT_PATTERN.matcher(rut.trim()).matches()) {
            return false;
        }
        String normalized = rut.trim().replace("-", "");
        String body = normalized.substring(0, normalized.length() - 1);
        char dv = Character.toUpperCase(normalized.charAt(normalized.length() - 1));
        return calcularDigitoVerificador(body) == dv;
    }

    /**
     * Calcula el dígito verificador (devuelve 'K' o un dígito).
     */
    public static char calcularDigitoVerificador(String body) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            suma += (body.charAt(i) - '0') * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = suma % 11;
        int dv = 11 - resto;
        if (dv == 11) {
            return '0';
        } else if (dv == 10) {
            return 'K';
        }
        return (char) ('0' + dv);
    }

    /**
     * Normaliza un RUT a formato "12345678-5". Lanza excepción si es inválido.
     */
    public static String normalize(String rut) {
        if (rut == null) {
            throw new IllegalArgumentException("El RUT es obligatorio");
        }
        String trimmed = rut.trim().replace(".", "");
        if (!isValid(trimmed)) {
            throw new IllegalArgumentException("RUT inválido: " + rut);
        }
        String normalized = trimmed.replace("-", "");
        String body = normalized.substring(0, normalized.length() - 1);
        char dv = normalized.charAt(normalized.length() - 1);
        return body + "-" + Character.toUpperCase(dv);
    }

    /**
     * Formatea un RUT normalizado ("15560647-9") al formato de miles y guion
     * "15.560.647-9". Si el RUT es nulo o inválido, lo devuelve tal cual.
     */
    public static String format(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;
        }
        // Quitar puntos para trabajar con el cuerpo normalizado (aunque venga ya formateado).
        String sinPuntos = rut.trim().replace(".", "");
        String cuerpo;
        char dv;
        if (sinPuntos.contains("-")) {
            int idx = sinPuntos.indexOf('-');
            cuerpo = sinPuntos.substring(0, idx);
            dv = sinPuntos.charAt(sinPuntos.length() - 1);
        } else {
            if (sinPuntos.length() < 2) {
                return rut;
            }
            cuerpo = sinPuntos.substring(0, sinPuntos.length() - 1);
            dv = sinPuntos.charAt(sinPuntos.length() - 1);
        }
        if (!cuerpo.matches("\\d+")) {
            return rut;
        }
        StringBuilder sb = new StringBuilder(cuerpo);
        for (int i = sb.length() - 3; i > 0; i -= 3) {
            sb.insert(i, '.');
        }
        return sb + "-" + Character.toUpperCase(dv);
    }
}
