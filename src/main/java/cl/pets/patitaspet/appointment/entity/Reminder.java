package cl.pets.patitaspet.appointment.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.pet.entity.Pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reminder {
    private Long id;
    private User user;
    private Pet pet; // puede ser null si aun no existe la mascota
    private String title;
    private String description;
    private LocalDate reminderDate;
    private Boolean isRecurring;
    private LocalDateTime createdAt;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}