// src/main/java/cl/pets/patitaspet/appointment/dto/AppointmentCreateRequest.java
package cl.pets.patitaspet.appointment.dto;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.pet.entity.Pet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentCreateRequest {

    private Long petId;
    private String title;
    private String appointmentDate; // ISO-8601 string
    private String notes;
    private String fcmToken;


    // getters y setters...


    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
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

    public PetAppointment toEntity() {
        PetAppointment appt = new PetAppointment();

        // 1) Asignamos la mascota con sólo el id
        Pet pet = new Pet();
        pet.setId(this.petId);
        appt.setPet(pet);

        // 2) Rellenamos el resto
        appt.setTitle(this.title);
        // parseamos el ISO string a LocalDateTime
        LocalDateTime dt = LocalDateTime.parse(
                this.appointmentDate,
                DateTimeFormatter.ISO_DATE_TIME
        );
        appt.setAppointmentDate(dt.toString());
        appt.setNotes(this.notes);

        return appt;
    }
}
