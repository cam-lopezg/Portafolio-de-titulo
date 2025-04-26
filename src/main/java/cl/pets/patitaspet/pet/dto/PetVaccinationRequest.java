package cl.pets.patitaspet.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetVaccinationRequest {
    private String vaccineName;
    private String description;
    private String dateGiven;
    private String nextDueDate;

    private Boolean multiDose;
    private Integer doseNumber;
    private Integer totalDoses;

    private String notes;
    private String veterinarianName;
}