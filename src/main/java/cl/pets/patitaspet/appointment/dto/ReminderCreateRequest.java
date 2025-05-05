// src/main/java/cl/pets/patitaspet/appointment/dto/ReminderCreateRequest.java
package cl.pets.patitaspet.appointment.dto;

public class ReminderCreateRequest {
    private long userId;
    private Long petId;
    private String title;
    private String description;
    // Cambiado de LocalDate a String:
    private String reminderDate;
    private Boolean recurring;
    private String fcmToken;

    // getters & setters
    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }



    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReminderDate() { return reminderDate; }
    public void setReminderDate(String reminderDate) { this.reminderDate = reminderDate; }

    public Boolean getRecurring() { return recurring; }
    public void setRecurring(Boolean recurring) { this.recurring = recurring; }
}
