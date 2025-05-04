package cl.pets.patitaspet.appointment.controller;


import cl.pets.patitaspet.appointment.dto.AppointmentCreateRequest;
import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.service.AppointmentService;
import cl.pets.patitaspet.pet.entity.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public String createAppointment(@RequestBody AppointmentCreateRequest request ) {
        validateAppointmentData(request.getPet(), request.getTitle(), request.getAppointmentDate());

        PetAppointment appointment = new PetAppointment();
        appointment.setPet(request.getPet());
        appointment.setTitle(request.getTitle());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setFcmToken(request.getFcmToken()); // nuevo


        Long appointmentId = appointmentService.saveAppointment(appointment);

        return "Cita creada exitosamente con ID: " + appointmentId;
    }
    @GetMapping
    public List<PetAppointment> listAllAppointments() {
        return appointmentService.getAllAppointments();
    }
    private void validateAppointmentData(Pet pet, String title, String appointmentDate) {
        if (pet == null || pet.getId() == null) {
            throw new IllegalArgumentException("Debe especificar una mascota válida.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la cita es obligatorio.");
        }
    }
}

