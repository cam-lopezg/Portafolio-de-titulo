package cl.pets.patitaspet.appointment.entity;

import cl.pets.patitaspet.pet.entity.Species;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportantDate {
    private Long id;
    private String name;
    private String date; // formato MM-DD
    private String description;
    private Species species;
}