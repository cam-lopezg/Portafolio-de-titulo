package cl.pets.patitaspet.appointment.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.pet.entity.Pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Reminder {
    private Long id;
    private User user;
    private Pet pet; // puede ser null si aun no existe la mascota
    private String title;
    private String description;
    private LocalDate reminderDate;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
}