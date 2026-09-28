package cl.ipfsoft.backend.mapper;

import cl.ipfsoft.backend.dto.schedule.ScheduleDayRequest;
import cl.ipfsoft.backend.dto.schedule.WeeklyScheduleRequest;
import cl.ipfsoft.backend.entity.Empleado;
import cl.ipfsoft.backend.entity.HorarioSemanal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * La etiqueta de la semana es opcional: el formulario la deja vacía y el
 * horario debe guardarse igual, sin dejar espacios sueltos en la base.
 */
class ScheduleMapperTest {

    private final ScheduleMapper mapper = new ScheduleMapper();

    private static WeeklyScheduleRequest request(String label) {
        return new WeeklyScheduleRequest(
                1L,
                LocalDate.of(2026, 9, 28),
                label,
                List.of(new ScheduleDayRequest(1, true,
                        LocalTime.of(9, 0), LocalTime.of(18, 0))));
    }

    private HorarioSemanal aplicar(String label) {
        Empleado empleado = new Empleado();
        empleado.setId(1L);
        HorarioSemanal ws = new HorarioSemanal();
        mapper.applyRequest(ws, request(label), empleado);
        return ws;
    }

    @Test
    @DisplayName("Guarda la etiqueta cuando viene informada")
    void guardaEtiquetaInformada() {
        assertEquals("Semana A", aplicar("Semana A").getLabel());
    }

    @Test
    @DisplayName("Acepta guardar sin etiqueta (null)")
    void aceptaEtiquetaNull() {
        assertNull(aplicar(null).getLabel());
    }

    @Test
    @DisplayName("Acepta guardar con la etiqueta vacía")
    void aceptaEtiquetaVacia() {
        assertNull(aplicar("").getLabel());
    }

    @Test
    @DisplayName("Una etiqueta solo con espacios se guarda como null")
    void etiquetaEnBlancoEsNull() {
        assertNull(aplicar("   ").getLabel());
    }

    @Test
    @DisplayName("Recorta los espacios sobrantes de la etiqueta")
    void recortaEtiqueta() {
        assertEquals("Semana B", aplicar("  Semana B  ").getLabel());
    }
}
