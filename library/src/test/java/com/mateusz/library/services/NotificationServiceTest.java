package com.mateusz.library.services;

import com.mateusz.library.TestUtils;
import com.mateusz.library.model.dao.NotificationEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.utils.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;



    @Test
    void shouldNotGetLoggedInUserAllNotifications_noAuthentication() {
        assertThrows(AccessDeniedException.class, () -> notificationService.getLoggedInUserAllNotifications());
    }

    @Test
    void shouldGetLoggedInUserAllNotifications() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        List<NotificationEntity> listOfNotifications = createListOfNotifications(userEntity);
        setNotificationsToAlreadyRead(listOfNotifications);
        userEntity.setNotifications(listOfNotifications);
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        //
        //Then
        //
        assertEquals(listOfNotifications, notificationService.getLoggedInUserAllNotifications());
    }
    @Test
    void shouldGetLoggedInUserCurrentNotifications() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        List<NotificationEntity> listOfNotifications = createListOfNotifications(userEntity);
        userEntity.setNotifications(listOfNotifications);
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        //
        //Then
        //
        assertEquals(duplicateListOfNotifications(listOfNotifications), notificationService.getLoggedInUserCurrentNotifications());
    }
    @Test
    void shouldDeleteNotifications() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        List<NotificationEntity> listOfNotifications = createListOfNotifications(userEntity);
        listOfNotifications.add(createAlreadyReadNotificationForUser(userEntity));
        userEntity.setNotifications(listOfNotifications);
        List<NotificationEntity> listOfRemovedNotifications = listOfNotifications.stream().filter(NotificationEntity::isAlreadyRead).toList();
        listOfRemovedNotifications.forEach(notification -> notification.setUserEntity(null));
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        //
        //Then
        //
        assertEquals(listOfRemovedNotifications, notificationService.deleteNotifications());
    }



    private void setNotificationsToAlreadyRead(List<NotificationEntity> listOfNotifications) {
        listOfNotifications.forEach(notification -> notification.setAlreadyRead(true));
    }

    private List<NotificationEntity> createListOfNotifications(UserEntity userEntity) {
        List<NotificationEntity> listOfNotifications = new ArrayList<>();
        NotificationEntity notification = createNotReadNotificationForUser(userEntity);
        listOfNotifications.add(notification);
        return listOfNotifications;
    }

    private NotificationEntity createNotReadNotificationForUser(UserEntity userEntity) {
        NotificationEntity notification = new NotificationEntity();
//        notification.setBookEntity(!userEntity.getRentedBooks().isEmpty() ? userEntity.getRentedBooks().get(0) : createBookForUser(userEntity));
        notification.setUserEntity(userEntity);
        notification.setAlreadyRead(false);
        notification.setReadingTimeByUser(DateUtils.parseDateToLocalDateTime(new Date()));
        return notification;
    }

    private NotificationEntity createAlreadyReadNotificationForUser(UserEntity userEntity) {
        NotificationEntity notification = createNotReadNotificationForUser(userEntity);
        notification.setAlreadyRead(true);
        return notification;
    }

    private List<NotificationEntity> duplicateListOfNotifications(List<NotificationEntity> listOfUserNotifications) {
        List<NotificationEntity> duplicatedList = new ArrayList<>();
        listOfUserNotifications.forEach(notification -> {
            NotificationEntity duplicatedNotification = NotificationEntity.builder()
                    .bookEntity(notification.getBookEntity())
                    .userEntity(notification.getUserEntity())
                    .creationTime(notification.getCreationTime())
                    .alreadyRead(notification.isAlreadyRead())
                    .readingTimeByUser(notification.getReadingTimeByUser())
                    .message(notification.getMessage())
                    .id(notification.getId())
                    .build();
            duplicatedList.add(duplicatedNotification);
        });
        return duplicatedList;
    }

    @AfterEach
    void clearContext(){
        SecurityContextHolder.clearContext();
    }
}
