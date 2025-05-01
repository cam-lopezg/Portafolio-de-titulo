package cl.pets.patitaspet.appointment.repository;


import cl.pets.patitaspet.appointment.entity.Reminder;

import java.util.List;

public interface FirestoreReminderRepository {
    String saveReminder(Reminder reminder);
    List<Reminder> findAllReminders();
}


