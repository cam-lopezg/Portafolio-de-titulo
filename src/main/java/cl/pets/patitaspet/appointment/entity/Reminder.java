package cl.pets.patitaspet.appointment.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.pet.entity.Pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reminder {
    private Long id;
    private Long userId;
    private Long petId; // puede ser null si aun no existe la mascota
    private String title;
    private String description;
    private LocalDate reminderDate;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
    private String fcmToken;


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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPet() {
        return petId;
    }

    public void setPet(Long petId) {
        this.petId = petId;
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


    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}