package cl.pets.patitaspet.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vaccine {
    private Long id;
    private String name;
    private Species species;
    private String description;
    private Integer recommendedAgeMonths;

}

