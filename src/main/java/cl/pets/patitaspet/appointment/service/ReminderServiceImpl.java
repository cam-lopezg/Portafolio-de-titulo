package cl.pets.patitaspet.appointment.service;


import cl.pets.patitaspet.appointment.entity.Reminder;
import cl.pets.patitaspet.appointment.repository.FirestoreReminderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final FirestoreReminderRepository reminderRepository;

    @Autowired
    public ReminderServiceImpl(FirestoreReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Override
    public Long saveReminder(Reminder reminder) {
        reminderRepository.saveReminder(reminder);
        return reminder.getId();
    }

    @Override
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAllReminders();
    }

    @Override
    public void deleteReminder(Long id) {
        reminderRepository.deleteReminder(id);
    }

}


