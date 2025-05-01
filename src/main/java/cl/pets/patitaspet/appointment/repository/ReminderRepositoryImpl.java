package cl.pets.patitaspet.appointment.repository;


import cl.pets.patitaspet.appointment.entity.Reminder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReminderRepositoryImpl implements ReminderRepository {

    private final FirestoreReminderRepository firestoreReminderRepository;

    @Autowired
    public ReminderRepositoryImpl(FirestoreReminderRepository firestoreReminderRepository) {
        this.firestoreReminderRepository = firestoreReminderRepository;
    }

    @Override
    public Long save(Reminder reminder) {
        firestoreReminderRepository.saveReminder(reminder);
        return reminder.getId();
    }

    @Override
    public List<Reminder> findAll() {
        return firestoreReminderRepository.findAllReminders();
    }
}
