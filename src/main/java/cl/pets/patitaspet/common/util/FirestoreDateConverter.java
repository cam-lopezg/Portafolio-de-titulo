package cl.pets.patitaspet.common.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Clase utilitaria para convertir objetos LocalDate y LocalDateTime a un
 * formato que Firestore pueda manejar sin problemas de serialización
 */
@Component
public class FirestoreDateConverter {

    private static final Logger logger = Logger.getLogger(FirestoreDateConverter.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convierte LocalDate a String para almacenar en Firestore
     */
    public String toString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Convierte LocalDateTime a String para almacenar en Firestore
     */
    public String toString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Convierte String a LocalDate al recuperar de Firestore
     */
    public LocalDate toLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            logger.warning("Error al convertir fecha: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Convierte String a LocalDateTime al recuperar de Firestore
     */
    public LocalDateTime toLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            logger.warning("Error al convertir fecha y hora: " + dateTimeStr + " - " + e.getMessage());
            return null;
        }
    }
}