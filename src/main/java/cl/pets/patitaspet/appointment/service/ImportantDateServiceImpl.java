package cl.pets.patitaspet.appointment.service;


import cl.pets.patitaspet.appointment.entity.ImportantDate;
import cl.pets.patitaspet.appointment.repository.ImportantDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ImportantDateServiceImpl implements ImportantDateService {

    @Autowired
    private final ImportantDateRepository importantDateRepository;

    public ImportantDateServiceImpl(ImportantDateRepository importantDateRepository) {
        this.importantDateRepository = importantDateRepository;
    }

    @Override
    public Long saveImportantDate(ImportantDate importantDate) {
        ImportantDate saved = importantDateRepository.save(importantDate);
        return saved.getId();
    }

    @Override
    public List<ImportantDate> getAllImportantDates() {
        return importantDateRepository.findAll();
    }
}


