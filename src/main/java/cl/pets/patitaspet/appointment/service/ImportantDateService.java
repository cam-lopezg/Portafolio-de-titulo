package cl.pets.patitaspet.appointment.service;


import cl.pets.patitaspet.appointment.entity.ImportantDate;

import java.util.List;

public interface ImportantDateService {
    Long saveImportantDate(ImportantDate importantDate);

    List<ImportantDate> getAllImportantDates();
}


