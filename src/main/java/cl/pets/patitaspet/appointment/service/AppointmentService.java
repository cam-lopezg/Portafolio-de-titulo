package cl.pets.patitaspet.appointment.service;

import cl.pets.patitaspet.appointment.entity.PetAppointment;

import java.util.List;

public interface AppointmentService {
    /** Guarda (o crea) la cita y devuelve la entidad con ID */
    PetAppointment saveAppointment(PetAppointment appointment);

    /** Devuelve todas las citas */
    List<PetAppointment> getAllAppointments();

    /** Devuelve las citas de una mascota concreta */
    List<PetAppointment> getAppointmentsByPet(Long petId);

    /** Elimina una cita por su ID */
    void deleteAppointment(Long appointmentId);
}
