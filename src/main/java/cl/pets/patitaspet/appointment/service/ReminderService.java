package cl.pets.patitaspet.appointment.service;

import cl.pets.patitaspet.appointment.entity.Reminder;

import java.util.List;

public interface ReminderService {

    Long saveReminder(Reminder reminder);
    List<Reminder> getAllReminders();


}


