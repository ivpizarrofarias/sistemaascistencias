package cl.ipfsoft.backend.service.impl;

import cl.ipfsoft.backend.entity.DiaHorario;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El PDF del horario debe identificar sin ambigüedad de quién es y a qué semana
 * corresponde: son los dos datos que se revisan al imprimirlo.
 */
class SchedulePdfServiceTest {

    private SchedulePdfService service;
    private String texto;

    @BeforeEach
    void setUp() throws IOException {
        service = new SchedulePdfService("IPFSoft SpA", "76.123.456-7");

        Empleado e = new Empleado();
        e.setId(3L);
        e.setRut("16876543-6");
        e.setFirstName("Javiera");
        e.setLastName("Muñoz");
        e.setCode("6454");

        HorarioSemanal ws = new HorarioSemanal();
        ws.setEmployee(e);
        ws.setStartDate(LocalDate.of(2026, 8, 31)); // lunes
        ws.setLabel("Semana A");

        List<DiaHorario> dias = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            DiaHorario d = new DiaHorario();
            d.setDayNumber(i);
            d.setWorked(i <= 5);
            d.setStartTime(LocalTime.of(9, 0));
            d.setEndTime(LocalTime.of(18, 0));
            dias.add(d);
        }
        ws.setDays(dias);

        texto = extraerTexto(service.generatePdf(ws));
    }

    /**
     * Extrae el texto del PDF descomprimiendo sus streams y juntando los
     * operandos de los operadores de texto de OpenPDF.
     */
    private static String extraerTexto(byte[] pdf) {
        StringBuilder sb = new StringBuilder();
        String raw = new String(pdf, StandardCharsets.ISO_8859_1);
        Pattern stream = Pattern.compile("stream\\r?\\n");
        Matcher m = stream.matcher(raw);
        while (m.find()) {
            int inicio = m.end();
            int fin = raw.indexOf("endstream", inicio);
            if (fin < 0) {
                break;
            }
            byte[] comprimido = raw.substring(inicio, fin).getBytes(StandardCharsets.ISO_8859_1);
            Inflater inflater = new Inflater();
            try {
                inflater.setInput(comprimido);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                while (!inflater.finished()) {
                    int n = inflater.inflate(buffer);
                    if (n == 0) {
                        break;
                    }
                    out.write(buffer, 0, n);
                }
                String contenido = out.toString(StandardCharsets.ISO_8859_1);
                Matcher txt = Pattern.compile("\\((.*?)\\)\\s*Tj").matcher(contenido);
                while (txt.find()) {
                    sb.append(txt.group(1)).append('\n');
                }
            } catch (DataFormatException ignored) {
                // El PDF tiene streams que no son texto (imágenes); se omiten.
            } finally {
                inflater.end();
            }
        }
        return sb.toString();
    }

    @Test
    @DisplayName("El PDF muestra el nombre completo del trabajador")
    void muestraNombreDelTrabajador() {
        assertTrue(texto.contains("Javiera Muñoz"),
                "El PDF debe mostrar el nombre del trabajador. Texto: " + texto);
    }

    @Test
    @DisplayName("El PDF muestra la semana laboral de lunes a sábado")
    void muestraSemanaLaboral() {
        assertTrue(texto.contains("Semana laboral del 31-08-2026 al 05-09-2026"),
                "El PDF debe mostrar la semana laboral. Texto: " + texto);
    }

    @Test
    @DisplayName("El PDF mantiene los datos del trabajador y la referencia")
    void mantieneDatosDelTrabajador() {
        assertTrue(texto.contains("16.876.543-6"));
        assertTrue(texto.contains("6454"));
        assertTrue(texto.contains("Semana A"));
    }

    @Test
    @DisplayName("El PDF sigue mostrando la tabla día por día")
    void muestraTablaDeDias() {
        assertTrue(texto.contains("LUNES"));
        assertTrue(texto.contains("SÁBADO"));
        assertTrue(texto.contains("09:00"));
        assertTrue(texto.contains("18:00"));
    }
}
