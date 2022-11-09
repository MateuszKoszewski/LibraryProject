package com.mateusz.library.controllers;

import com.mateusz.library.model.dao.NotificationEntity;
import com.mateusz.library.services.NotificationService;
import com.mateusz.library.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/getAllNotifications")
    public ResponseEntity<List<NotificationEntity>> getUserAllNotifications() {
        List<NotificationEntity> userNotifications = notificationService.getLoggedInUserAllNotifications();
        return new ResponseEntity<>(userNotifications, HttpStatus.OK);
    }

    @GetMapping("/getCurrentNotifications")
    public ResponseEntity<List<NotificationEntity>> getUserCurrentNotifications() {
        List<NotificationEntity> userNotifications = notificationService.getLoggedInUserCurrentNotifications();
        return new ResponseEntity<>(userNotifications, HttpStatus.OK);
    }

    @DeleteMapping("/deleteNotifications")
    public ResponseEntity<List<NotificationEntity>> deleteNotifications() {
        List<NotificationEntity> userNotifications = notificationService.deleteNotifications();
        return new ResponseEntity<>(userNotifications, HttpStatus.OK);
    }
}
