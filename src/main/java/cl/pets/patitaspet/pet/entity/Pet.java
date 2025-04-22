package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.entity.Reminder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Pet {
    private Long id;
    private User user;
    private String name;
    private Species species;
    private Breed breed;
    private LocalDate birthdate;
    private Gender gender;
    private String photoUrl;
    private LocalDateTime createdAt;

    private List<PetVaccination> vaccinations;
    private List<Reminder> reminders;
    private List<PetAppointment> appointments;
    private List<PetMedication> medications;
}