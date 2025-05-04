package cl.pets.patitaspet.appointment.dto;

import cl.pets.patitaspet.pet.entity.Pet;

public class AppointmentCreateRequest {

    //Pet pet, String title, LocalDate appointmentDate, String location, String notes

    private Pet pet;
    private String title;
    private String appointmentDate;
    private String notes;
    private String fcmToken; // ✅ nuevo campo


    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
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

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
