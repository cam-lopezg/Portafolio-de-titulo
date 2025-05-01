package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.Reminder;

import java.util.List;

public interface ReminderRepository {
    Long save(Reminder reminder);
    List<Reminder> findAll();
}


