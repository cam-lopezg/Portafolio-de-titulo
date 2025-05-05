package cl.pets.patitaspet.notifications.scheduler;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.entity.Reminder;
import cl.pets.patitaspet.appointment.entity.ImportantDate;
import cl.pets.patitaspet.appointment.service.AppointmentService;
import cl.pets.patitaspet.appointment.service.ReminderService;
import cl.pets.patitaspet.appointment.service.ImportantDateService;
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
    private ReminderService reminderService;

    @Autowired
    private ImportantDateService importantDateService;

    @Autowired
    private FcmNotificationService fcmService;

    public ReminderScheduler(
            AppointmentService appointmentService,
            ReminderService reminderService,
            ImportantDateService importantDateService,
            FcmNotificationService fcmService) {
        this.appointmentService = appointmentService;
        this.reminderService = reminderService;
        this.importantDateService = importantDateService;
        this.fcmService = fcmService;
    }

    @Scheduled(cron = "0 0 8 * * *") // Todos los días a las 8 AM
    public void revisarFechasYNotificar() {
        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);

        notificarCitas(manana);
        notificarRecordatorios(manana);
        notificarFechasImportantes(hoy);
    }

    // 1. Notificar citas médicas
    private void notificarCitas(LocalDate objetivo) {
        List<PetAppointment> citas = appointmentService.getAllAppointments();

        for (PetAppointment cita : citas) {
            LocalDate fecha = LocalDate.parse(cita.getAppointmentDate());
            if (fecha.equals(objetivo)) {
                enviar(cita.getFcmToken(), "Cita para " + cita.getPet().getName(), "Tu mascota tiene una cita mañana: " + cita.getNotes());
            }
        }
    }

    // 2. Notificar recordatorios personales
    private void notificarRecordatorios(LocalDate objetivo) {
        List<Reminder> reminders = reminderService.getAllReminders();

        for (Reminder r : reminders) {
            LocalDate fecha = LocalDate.parse(r.getReminderDate());
            if (fecha.equals(objetivo)) {
                enviar(r.getFcmToken(), "Recordatorio: " + r.getTitle(), r.getDescription());
            }
        }
    }

    // 3. Notificar fechas importantes por especie
    private void notificarFechasImportantes(LocalDate objetivo) {
        String hoyMMDD = String.format("%02d-%02d", objetivo.getMonthValue(), objetivo.getDayOfMonth());

        List<ImportantDate> fechas = importantDateService.getAllImportantDates();

        for (ImportantDate date : fechas) {
            if (date.getDate().equals(hoyMMDD)) {
                enviar(date.getFcmToken(), "Hoy es: " + date.getName(), date.getDescription());
            }
        }
    }

    // Método centralizado de envío
    private void enviar(String token, String title, String body) {
        if (token != null && !token.trim().isEmpty()) {
            try {
                fcmService.sendToToken(token, title, body);
            } catch (Exception e) {
                System.err.println("❌ Error al enviar notificación: " + e.getMessage());
            }
        }
    }
}
