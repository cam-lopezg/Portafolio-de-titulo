package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.ImportantDate;

import java.util.List;

public interface FirestoreImportantDateRepository {
    String saveImportantDate(ImportantDate importantDate);
    List<ImportantDate> findAllImportantDates();
}

