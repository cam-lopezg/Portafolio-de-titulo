package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.PetAppointment;

import java.util.List;
import java.util.Optional;

public interface FirestoreAppointmentRepository {
    public String saveAppointment(PetAppointment appointment);
    public Optional<PetAppointment> findAppointmentById(String appointmentId);

    List<PetAppointment> findAppointmentsByPetId(Long petId);

    void deleteAppointment(String appointmentId);

    List<PetAppointment> findAllAppointments();
}
