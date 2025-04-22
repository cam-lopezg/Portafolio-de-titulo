package cl.pets.patitaspet.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase de utilidad para operaciones comunes con fechas
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtil() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Formatea una fecha en formato dd/MM/yyyy, estas se hiciceron asi para evitar
     * el exceso de info en la bdd
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATTER.format(date);
    }

    /**
     * Formatea una fecha y hora en formato dd/MM/yyyy HH:mm
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATETIME_FORMATTER.format(dateTime);
    }

    /**
     * Calcula la edad en meses desde una fecha hasta hoy
     */
    public static int calculateMonthsFromDate(LocalDate date) {
        if (date == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        int years = now.getYear() - date.getYear();
        int months = now.getMonthValue() - date.getMonthValue();

        return years * 12 + months;
    }
}