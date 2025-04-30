package cl.pets.patitaspet.appointment.controller;


import cl.pets.patitaspet.appointment.dto.ImportantDateCreateRequest;
import cl.pets.patitaspet.appointment.entity.ImportantDate;
import cl.pets.patitaspet.appointment.service.ImportantDateService;
import cl.pets.patitaspet.pet.entity.Species;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/important-date")
public class ImportantDateController {

    @Autowired
    private final ImportantDateService importantDateService;

    public ImportantDateController(ImportantDateService importantDateService) {
        this.importantDateService = importantDateService;
    }

    @PostMapping
    public String createImportantDate(@RequestBody ImportantDateCreateRequest request) {
        validateImportantDateData(request.getName(), request.getDate(), request.getSpecies());

        ImportantDate importantDate = new ImportantDate();
        importantDate.setName(request.getName());
        importantDate.setDate(request.getDate());
        importantDate.setDescription(request.getDescription());
        importantDate.setSpecies(request.getSpecies());

        Long importantDateId = importantDateService.saveImportantDate(importantDate);

        return "Fecha importante creada exitosamente con ID: " + importantDateId;
    }


    @GetMapping
    public List<ImportantDate> listAllImportantDates() {
        return importantDateService.getAllImportantDates();
    }

    private void validateImportantDateData(String name, String date, Species species) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la fecha importante es obligatorio.");
        }
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar una fecha en formato MM-DD.");
        }
        if (species == null) {
            throw new IllegalArgumentException("Debe especificar una especie válida.");
        }
    }
}


