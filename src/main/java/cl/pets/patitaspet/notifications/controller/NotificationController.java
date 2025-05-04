package cl.pets.patitaspet.notifications.controller;

import cl.pets.patitaspet.notifications.service.FcmNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final FcmNotificationService fcmService;

    public NotificationController(FcmNotificationService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody Map<String, String> body) {
        try {
            fcmService.sendToToken(body.get("token"), body.get("title"), body.get("body"));
            return ResponseEntity.ok("Notificación enviada");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
