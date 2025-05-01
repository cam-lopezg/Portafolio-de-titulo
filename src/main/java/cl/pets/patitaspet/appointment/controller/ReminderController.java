package cl.pets.patitaspet.appointment.controller;


import cl.pets.patitaspet.appointment.dto.ReminderCreateRequest;
import cl.pets.patitaspet.appointment.entity.Reminder;
import cl.pets.patitaspet.appointment.service.ReminderService;

import cl.pets.patitaspet.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminder")
public class ReminderController {

    @Autowired
    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public String createReminder(@RequestBody ReminderCreateRequest request) {
        validateReminderData(request.getUser(), request.getTitle(), request.getReminderDate());

        Reminder reminder = new Reminder();
        reminder.setUser(request.getUser());
        reminder.setPet(request.getPet()); // puede ser null
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setIsRecurring(request.getIsRecurring());
        reminder.setCreatedAt(LocalDateTime.now());

        Long reminderId = reminderService.saveReminder(reminder);

        return "Recordatorio creado exitosamente con ID: " + reminderId;
    }

    @GetMapping
    public List<Reminder> listAllReminders() {
        return reminderService.getAllReminders();
    }

    private void validateReminderData(User user, String title, LocalDate reminderDate) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Debe especificar un usuario válido.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del recordatorio es obligatorio.");
        }
        if (reminderDate == null) {
            throw new IllegalArgumentException("Debe especificar una fecha de recordatorio.");
        }
    }
}


