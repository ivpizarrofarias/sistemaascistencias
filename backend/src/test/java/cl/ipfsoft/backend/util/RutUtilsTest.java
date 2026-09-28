package cl.ipfsoft.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del util de validación y normalización del RUT chileno.
 *
 * Los dígitos verificadores salen de la serie oficial del SII: se toman los
 * dígitos de derecha a izquierda y se multiplican por 2, 3, 4, 5, 6 y 7; al
 * terminar la serie se vuelve a 2. Luego DV = 11 - (suma % 11), donde 11 es 0
 * y 10 es K.
 *
 * Los casos secontrastaron con los ejemplos publicados del algoritmo
 * (30.686.957-4, 27.962.409-2, 13.537.201-3, 1.091.397-7, 11.111.111-1).
 */
class RutUtilsTest {

    @Test
    @DisplayName("RUT válido con guion se acepta")
    void rutValidoConGuion_seAcepta() {
        assertTrue(RutUtils.isValid("11111111-1"));
    }

    @Test
    @DisplayName("RUT válido sin guion se acepta")
    void rutValidoSinGuion_seAcepta() {
        assertTrue(RutUtils.isValid("111111111"));
    }

    @Test
    @DisplayName("RUT válido con puntos de miles se acepta")
    void rutValidoConPuntos_seAcepta() {
        assertTrue(RutUtils.isValid("12.345.678-5"));
        assertTrue(RutUtils.isValid("1.234.567-4"));
    }

    @Test
    @DisplayName("RUT con dígito K se acepta")
    void rutValidoConK_seAcepta() {
        assertTrue(RutUtils.isValid("21365487-K"), "Los RUT que verifican en K también son válidos");
        assertTrue(RutUtils.isValid("7600000-K"));
    }

    @Test
    @DisplayName("Dígito verificador incorrecto se rechaza")
    void rutIncorrecto_seRechaza() {
        // 11111111 verifica en 1, no en 9 ni en 2.
        assertFalse(RutUtils.isValid("11111111-9"));
        assertFalse(RutUtils.isValid("11111111-2"));
        // 12.345.678 verifica en 5, no en 9.
        assertFalse(RutUtils.isValid("12345678-9"));
    }

    @Test
    @DisplayName("Formato inválido se rechaza")
    void formatoInvalido_seRechaza() {
        assertFalse(RutUtils.isValid("123"));
        assertFalse(RutUtils.isValid("abc-def"));
        assertFalse(RutUtils.isValid("1111111111111"), "Demasiados dígitos");
        assertFalse(RutUtils.isValid(null));
        assertFalse(RutUtils.isValid(""));
        assertFalse(RutUtils.isValid("   "));
    }

    @Test
    @DisplayName("El dígito verificador sigue el algoritmo oficial del SII")
    void calcularDigitoVerificador_algoritmoOficial() {
        // Los cuatro primeros son ejemplos publicados del algoritmo.
        assertEquals('4', RutUtils.calcularDigitoVerificador("30686957"));
        assertEquals('2', RutUtils.calcularDigitoVerificador("27962409"));
        assertEquals('3', RutUtils.calcularDigitoVerificador("13537201"));
        assertEquals('7', RutUtils.calcularDigitoVerificador("1091397"));
        assertEquals('1', RutUtils.calcularDigitoVerificador("11111111"));
        assertEquals('5', RutUtils.calcularDigitoVerificador("12345678"));
        assertEquals('K', RutUtils.calcularDigitoVerificador("21365487"));
        // 10.000.004 -> suma 44 -> 44%11=0 -> 11-0=11 -> DV 0
        assertEquals('0', RutUtils.calcularDigitoVerificador("10000004"));
    }

    @Test
    void normalize_formateaCorrectamente() {
        assertEquals("11111111-1", RutUtils.normalize("11111111-1"));
        assertEquals("11111111-1", RutUtils.normalize("11.111.111-1"));
        assertEquals("12345678-5", RutUtils.normalize("12.345.678-5"));
        assertEquals("12345678-5", RutUtils.normalize("123456785"));
    }

    @Test
    void format_agregaPuntosDeMiles() {
        assertEquals("15.560.647-9", RutUtils.format("15560647-9"));
        assertEquals("11.111.111-1", RutUtils.format("11111111-1"));
        assertEquals("7.600.000-K", RutUtils.format("7600000-K"));
    }

    @Test
    void format_idempotente() {
        assertEquals("15.560.647-9", RutUtils.format("15.560.647-9"));
    }

    @Test
    void format_nullOVacio_devuelveOriginal() {
        assertEquals(null, RutUtils.format(null));
        assertEquals("", RutUtils.format(""));
        assertEquals("abc", RutUtils.format("abc"));
    }

    @Test
    void normalize_rutInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> RutUtils.normalize("11111111-9"));
    }
}
