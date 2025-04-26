package cl.pets.patitaspet.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetVaccinationResponse {
    private Long id;
    private Long petId;
    private String petName; // Nombre de la mascota para mostrar en el frontend

    private String vaccineName;
    private String description;
    private String dateGivenStr;
    private String nextDueDateStr;

    private Boolean multiDose;
    private Integer doseNumber;
    private Integer totalDoses;

    private String notes;
    private String veterinarianName;
    private String createdAtStr;
}