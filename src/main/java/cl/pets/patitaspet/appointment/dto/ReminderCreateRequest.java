package cl.pets.patitaspet.appointment.dto;


import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.user.entity.User;

import java.time.LocalDate;

public class ReminderCreateRequest {

    private User user;
    private Pet pet; // puede ser null
    private String title;
    private String description;
    private LocalDate reminderDate;
    private Boolean isRecurring;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
}

