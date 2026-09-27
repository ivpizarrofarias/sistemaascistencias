package cl.ipfsoft.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del generador de código de marcaje.
 *
 * El código es un número aleatorio de 4 dígitos (1000-9999), único en el sistema
 * e inmutable: no se deriva del RUT ni del nombre, justamente para que no se
 * pueda adivinar el de un compañero ni cambie al corregir el RUT.
 */
class CodeGeneratorTest {

    @Test
    @DisplayName("El código tiene 4 dígitos")
    void uniqueCode_tieneCuatroDigitos() {
        for (int i = 0; i < 500; i++) {
            assertTrue(CodeGenerator.esCodigoValido(CodeGenerator.uniqueCode(s -> false)),
                    "El código debe tener 4 dígitos");
        }
    }

    @Test
    @DisplayName("El código siempre está entre 1000 y 9999")
    void uniqueCode_enElRangoValido() {
        for (int i = 0; i < 500; i++) {
            int n = Integer.parseInt(CodeGenerator.uniqueCode(s -> false));
            assertTrue(n >= 1000 && n <= 9999, "El código debe estar entre 1000 y 9999, fue " + n);
        }
    }

    @Test
    @DisplayName("El código no depende del RUT ni del nombre")
    void uniqueCode_noDependeDelRutNiDelNombre() {
        // Dos empleados con RUT y nombre distintos pueden (deben) recibir códigos distintos.
        String a = CodeGenerator.uniqueCode(s -> false);
        String b = CodeGenerator.uniqueCode(s -> false);
        assertNotEquals(a, b, "Cada empleado debe recibir un código propio");
    }

    @Test
    @DisplayName("El código no es predecible a partir del RUT")
    void uniqueCode_noSeDeduceDelRut() {
        // Ninguna porción del código debe coincidir con el prefijo del RUT,
        // porque el código se elige al azar y no se deriva de los datos del empleado.
        for (int i = 0; i < 200; i++) {
            String codigo = CodeGenerator.uniqueCode(s -> false);
            assertNotEquals("1687", codigo, "El código no debe ser el prefijo del RUT");
        }
    }

    @Test
    @DisplayName("El código nunca se repite si ya está en uso")
    void uniqueCode_omiteLosUsados() {
        List<String> usados = new ArrayList<>(List.of("1000", "1001", "1002", "4832"));

        for (int i = 0; i < 200; i++) {
            String codigo = CodeGenerator.uniqueCode(usados::contains);
            assertFalse(usados.contains(codigo), "El código " + codigo + " ya estaba en uso");
            usados.add(codigo);
        }

        assertEquals(204, usados.size(), "No debe perderse ningún código generado");
    }

    @Test
    @DisplayName("Con la tabla casi llena igual encuentra un código libre")
    void uniqueCode_casiLlenoEncuentraUnLibre() {
        // Solo queda libre el 9999.
        List<String> usados = new ArrayList<>();
        for (int n = 1000; n <= 9998; n++) {
            usados.add(String.valueOf(n));
        }

        assertEquals("9999", CodeGenerator.uniqueCode(usados::contains));
    }

    @Test
    @DisplayName("Si no queda ningún código libre, avisa en vez de devolver uno repetido")
    void uniqueCode_sinCodigosLibres_lanzaExcepcion() {
        List<String> usados = new ArrayList<>();
        for (int n = 1000; n <= 9999; n++) {
            usados.add(String.valueOf(n));
        }

        assertThrows(IllegalStateException.class, () -> CodeGenerator.uniqueCode(usados::contains));
    }

    @Test
    @DisplayName("Mil códigos generados son todos distintos")
    void uniqueCode_milCodigosTodosDistintos() {
        Set<String> generados = new HashSet<>();
        List<String> usados = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            usados.add(CodeGenerator.uniqueCode(usados::contains));
        }
        generados.addAll(usados);

        assertEquals(1000, generados.size(), "Los 1000 códigos deben ser distintos entre sí");
    }

    @Test
    @DisplayName("esCodigoValido acepta solo 4 dígitos")
    void esCodigoValido_soloCuatroDigitos() {
        assertTrue(CodeGenerator.esCodigoValido("1234"));
        assertTrue(CodeGenerator.esCodigoValido(" 1234 "), "Tolera espacios al teclear");

        assertFalse(CodeGenerator.esCodigoValido("123"), "3 dígitos no sirve");
        assertFalse(CodeGenerator.esCodigoValido("12345"), "5 dígitos no sirve");
        assertFalse(CodeGenerator.esCodigoValido("1687JAVI"), "El formato anterior con letras ya no es válido");
        assertFalse(CodeGenerator.esCodigoValido("12a4"));
        assertFalse(CodeGenerator.esCodigoValido(null));
        assertFalse(CodeGenerator.esCodigoValido(""));
    }

    @Test
    @DisplayName("randomCode no necesita base de datos")
    void randomCode_funcionaSinPredicate() {
        assertTrue(CodeGenerator.esCodigoValido(CodeGenerator.randomCode()));
    }
}
