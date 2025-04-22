package cl.pets.patitaspet.appointment.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.pet.entity.Pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class PetAppointment {
    private Long id;
    private Pet pet;
    private String title;
    private LocalDate appointmentDate;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
}