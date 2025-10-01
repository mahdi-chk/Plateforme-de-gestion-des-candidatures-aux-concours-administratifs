package com.concours.controller;

import com.concours.service.EmailService;
import com.concours.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {


    private final EmailService emailService;

    public NotificationController(NotificationService notificationService, EmailService emailService) {

        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<Void> sendTestNotification(
            @RequestParam String email, @RequestParam String message) {
        emailService.envoyerNotification(email, "Test", message);
        return ResponseEntity.ok().build();
    }
}