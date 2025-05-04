package cl.pets.patitaspet.notifications.scheduler;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.service.AppointmentService;
import cl.pets.patitaspet.notifications.service.FcmNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private FcmNotificationService fcmService;

    public ReminderScheduler(AppointmentService appointmentService, FcmNotificationService fcmService) {
        this.appointmentService = appointmentService;
        this.fcmService = fcmService;
    }

    @Scheduled(cron = "0 0 8 * * *") // cada día a las 8 AM
    public void revisarCitasYNotificar() {
        List<PetAppointment> todasLasCitas = appointmentService.getAllAppointments();
        LocalDate mañana = LocalDate.now().plusDays(1);

        for (PetAppointment cita : todasLasCitas) {
            LocalDate fechaCita = LocalDate.parse(cita.getAppointmentDate()); // yyyy-MM-dd

            if (fechaCita.equals(mañana)) {
                String token = cita.getFcmToken();
                if (token != null && !token.isEmpty()) {
                    String title = "Recordatorio de cita para " + cita.getPet().getName();
                    String body = "Tu mascota tiene una cita mañana en: " + cita.getNotes();

                    try {
                        fcmService.sendToToken(token, title, body);
                    } catch (Exception e) {
                        System.err.println("Error al enviar notificación: " + e.getMessage());
                    }
                }
            }
        }
    }
}

