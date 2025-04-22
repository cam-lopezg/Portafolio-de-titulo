package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PetMedication {
    private Long id;
    private Pet pet;
    private String medicationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dosageInstructions;
    private String notes;
}