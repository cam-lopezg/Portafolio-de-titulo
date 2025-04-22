package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Breed {
    private Long id;
    private String name;
    private Species species;
    private String description;
}