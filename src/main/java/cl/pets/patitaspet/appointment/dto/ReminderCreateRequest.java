package cl.pets.patitaspet.appointment.dto;


import cl.pets.patitaspet.appointment.entity.Reminder;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.user.entity.User;

import java.time.LocalDate;

public class ReminderCreateRequest {

    private Long userId;
    private Long petId;
    private String title;
    private String description;
    private LocalDate reminderDate;
    private Boolean isRecurring;
    private String fcmToken; // 🔔 Necesario para notificaciones

    // Getters y Setters...


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
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

    public Boolean getRecurring() {
        return isRecurring;
    }

    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
}


