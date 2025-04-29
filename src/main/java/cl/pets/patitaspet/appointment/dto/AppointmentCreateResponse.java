package cl.pets.patitaspet.appointment.dto;

import cl.pets.patitaspet.pet.entity.Pet;

public class AppointmentCreateResponse {

    private Long id;
    private String message;

    public AppointmentCreateResponse() {
    }

    public AppointmentCreateResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}



