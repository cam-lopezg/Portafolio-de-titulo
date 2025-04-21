package cl.pets.patitaspet.entity;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
public class Reminder {
    private Long id;
    private User user;
    private Pet pet; // puede ser null
    private String title;
    private String description;
    private LocalDate reminderDate;
    private Boolean isRecurring;
    private LocalDateTime createdAt;

}

