package cl.ipfsoft.backend.util;

import cl.ipfsoft.backend.entity.ScheduleDay;
import cl.ipfsoft.backend.entity.WeeklySchedule;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Utilidades para trabajar con los horarios semanales ({@link WeeklySchedule}).
 */
public final class ScheduleUtils {

    private ScheduleUtils() {
    }

    /** Devuelve el lunes de la semana a la que pertenece una fecha. */
    public static LocalDate mondayOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Indica si un día de la semana (1=lunes ... 6=sábado) está marcado como
     * trabajado en el horario. Si no existe horario se usa {@code defaultNoSchedule}.
     */
    public static boolean isDayWorked(WeeklySchedule ws, int dayNumber, boolean defaultNoSchedule) {
        if (ws == null) {
            return defaultNoSchedule;
        }
        return ws.getDays().stream()
                .filter(d -> d.getDayNumber() == dayNumber)
                .findFirst()
                .map(ScheduleDay::isWorked)
                .orElse(defaultNoSchedule);
    }

    /**
     * Calcula la duración media (en horas, redondeada) de los días trabajados de
     * la semana. Si no hay horario con horas definidas, usa {@code defaultHours}.
     */
    public static int hoursPerDay(WeeklySchedule ws, int defaultHours) {
        if (ws == null) {
            return defaultHours;
        }
        double total = 0;
        int n = 0;
        for (ScheduleDay d : ws.getDays()) {
            if (d.isWorked() && d.getStartTime() != null && d.getEndTime() != null) {
                total += Duration.between(d.getStartTime(), d.getEndTime()).toMinutes() / 60.0;
                n++;
            }
        }
        return n > 0 ? (int) Math.round(total / n) : defaultHours;
    }
}
