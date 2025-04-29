package cl.pets.patitaspet.appointment.service;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
import java.util.List;

public interface AppointmentService {

    Long saveAppointment(PetAppointment appointment);
    List<PetAppointment> getAllAppointments(); // Nuevo método agregado
}





