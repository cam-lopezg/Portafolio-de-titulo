package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PetVaccination {
    private Long id;
    private Pet pet;
    private Vaccine vaccine;
    private LocalDate dateGiven;
    private LocalDate nextDueDate;
    private String notes;
}