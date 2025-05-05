package cl.pets.patitaspet.appointment.service;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.repository.FirestoreAppointmentRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final FirestoreAppointmentRepositoryImpl repo;

    @Autowired
    public AppointmentServiceImpl(FirestoreAppointmentRepositoryImpl repo) {
        this.repo = repo;
    }

    @Override
    public PetAppointment saveAppointment(PetAppointment appointment) {
        repo.saveAppointment(appointment);
        return appointment;
    }

    @Override
    public List<PetAppointment> getAllAppointments() {
        return repo.findAllAppointments();
    }

    @Override
    public List<PetAppointment> getAppointmentsByPet(Long petId) {
        return repo.findAppointmentsByPetId(petId);
    }

    @Override
    public void deleteAppointment(Long appointmentId) {
        repo.deleteAppointment(appointmentId.toString());
    }
}
