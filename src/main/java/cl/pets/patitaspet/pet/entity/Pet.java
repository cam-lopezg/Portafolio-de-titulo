package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.entity.Reminder;

import java.util.List;

@Getter
@Setter
public class Pet {
    private Long id;
    // Se enlaza solo el ID del dueño para optimizar la bdd
    private Long userId;
    // Opcional: podemos mantener algunos datos básicos para mostrar sin necesidad
    // de consultas adicionales
    private String ownerName; // Esto es solo para mostrar información básica en la bdd, no se usa en la app

    private String name;
    private Species species;
    private Breed breed;

    // Usar solo String para fechas - esto elimina el problema de serialización con
    // Firestore
    private String birthdateStr;
    private Gender gender;
    private String photoUrl;
    private String createdAtStr;

    private List<PetVaccination> vaccinations;
    private List<Reminder> reminders;
    private List<PetAppointment> appointments;
    private List<PetMedication> medications;
}