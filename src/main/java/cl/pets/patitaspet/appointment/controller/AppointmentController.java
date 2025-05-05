package cl.pets.patitaspet.appointment.controller;

import cl.pets.patitaspet.appointment.dto.AppointmentCreateRequest;
import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createAppointment(@RequestBody AppointmentCreateRequest request) {
        // Mapeo y guardado como antes
        PetAppointment appointment = appointmentService.saveAppointment(request.toEntity());

        // Devuelvo un objeto que Spring convierte a JSON
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", appointment.getId());
        resp.put("message", "Cita creada exitosamente");
        return resp;
    }
    /** Listar todas las citas (para ADMIN u otros) */
    @GetMapping
    public ResponseEntity<List<PetAppointment>> listAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /** Listar sólo las citas de una mascota */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<PetAppointment>> getByPet(@PathVariable Long petId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPet(petId));
    }

    /** Eliminar una cita por ID */
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long appointmentId) {
        System.out.println("eliminar cita: " + appointmentId);
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
