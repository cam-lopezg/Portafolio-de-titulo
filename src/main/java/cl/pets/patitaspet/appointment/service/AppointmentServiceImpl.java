package cl.pets.patitaspet.appointment.service;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.repository.FirestoreAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final FirestoreAppointmentRepository firestoreAppointmentRepository;

    @Autowired
    public AppointmentServiceImpl(FirestoreAppointmentRepository firestoreAppointmentRepository) {
        this.firestoreAppointmentRepository = firestoreAppointmentRepository;
    }

    @Override
    public Long saveAppointment(PetAppointment appointment) {
        firestoreAppointmentRepository.saveAppointment(appointment);
        return appointment.getId(); // El ID ya lo asigna Firestore basado en System.currentTimeMillis() o UUID
    }

    @Override
    public List<PetAppointment> getAllAppointments() {
        return firestoreAppointmentRepository.findAllAppointments();
    }
}




