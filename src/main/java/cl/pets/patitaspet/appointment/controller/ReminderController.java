// src/main/java/cl/pets/patitaspet/appointment/controller/ReminderController.java
package cl.pets.patitaspet.appointment.controller;

import cl.pets.patitaspet.appointment.dto.ReminderCreateRequest;
import cl.pets.patitaspet.appointment.entity.Reminder;
import cl.pets.patitaspet.appointment.service.ReminderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/reminder")
public class ReminderController {

    private final ReminderService reminderService;

    @Autowired
    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * Crea un nuevo recordatorio y devuelve un JSON con éxito, el ID y un mensaje.
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String,Object>> createReminder(
            @RequestBody ReminderCreateRequest request) {

        // Validaciones básicas
        validateReminderData(request.getUserId(), request.getTitle(), request.getReminderDate());

        // Construye la entidad
        Reminder reminder = new Reminder();
        reminder.setUserId(request.getUserId());
        reminder.setPet(request.getPetId());    // puede ser null
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        // Aquí guardamos la fecha tal cual vino como String
        reminder.setReminderDate(request.getReminderDate());
        reminder.setIsRecurring(request.getRecurring());
        reminder.setCreatedAt(LocalDateTime.now().toString());
        reminder.setFcmToken(request.getFcmToken());

        // Persiste y obtiene el ID generado
        Long reminderId = reminderService.saveReminder(reminder);

        // Prepara la respuesta JSON
        Map<String,Object> body = new HashMap<>();
        body.put("success", true);
        body.put("id", reminderId);
        body.put("message", "Recordatorio creado exitosamente");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }

    /**
     * Lista todos los recordatorios existentes.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Reminder>> listAllReminders() {
        List<Reminder> all = reminderService.getAllReminders();
        return ResponseEntity.ok(all);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);

        Map<String,Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Recordatorio eliminado exitosamente");
        return ResponseEntity.ok(body);
    }

    /**
     * Valida que vengan los datos mínimos.
     */
    private void validateReminderData(long userId, String title, String reminderDate) {
        if (userId == 0) {
            throw new IllegalArgumentException("Debe especificar un usuario válido.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del recordatorio es obligatorio.");
        }
        if (reminderDate == null || reminderDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar una fecha de recordatorio.");
        }
    }
}
