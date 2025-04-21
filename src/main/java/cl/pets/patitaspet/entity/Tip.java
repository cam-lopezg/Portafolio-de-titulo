package cl.pets.patitaspet.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Tip {
    private Long id;
    private String title;
    private String description;
    private Species species;
    private Integer minAgeMonths;
    private Integer maxAgeMonths;
    private Breed breed; // puede ser null

}

