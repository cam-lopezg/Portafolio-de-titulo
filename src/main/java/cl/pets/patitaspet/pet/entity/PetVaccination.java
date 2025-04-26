package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetVaccination {
    private Long id;
    private Long petId; // Referencia al ID de la mascota para optimizar

    private String vaccineName; // Nombre de la vacuna
    private String description; // Descripción de la vacuna
    private String dateGivenStr; // Fecha en que se aplicó la vacuna (formato String)
    private String nextDueDateStr; // Fecha para la próxima dosis, si aplica (formato String)

    private boolean multiDose; // Indica si es una vacuna de múltiples dosis
    private Integer doseNumber; // Número de la dosis actual (1, 2, 3...)
    private Integer totalDoses; // Total de dosis requeridas

    private String notes; // Notas adicionales (reacciones, observaciones, etc.)
    private String veterinarianName; // Nombre del veterinario que aplicó la vacuna
    private String createdAtStr; // Fecha de creación del registro
}