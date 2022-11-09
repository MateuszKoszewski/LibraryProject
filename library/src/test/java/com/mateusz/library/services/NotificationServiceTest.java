package com.mateusz.library.services;

import com.mateusz.library.TestUtils;
import com.mateusz.library.constants.NotificationMessages;
import com.mateusz.library.model.dao.NotificationEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
        List<NotificationEntity> listOfAlreadyReadNotifications = createAlreadyReadNotifications(userEntity);
        userEntity.setNotifications(listOfAlreadyReadNotifications);
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        //
        //Then
        //
        assertEquals(listOfAlreadyReadNotifications, notificationService.getLoggedInUserAllNotifications());
    }



    private List<NotificationEntity> createAlreadyReadNotifications(UserEntity userEntity) {
        List<NotificationEntity> listOfNotifications = createListOfNotifications(userEntity);
        listOfNotifications.forEach(notification -> notification.setAlreadyRead(true));
        return listOfNotifications;
    }

    private List<NotificationEntity> createListOfNotifications(UserEntity userEntity) {
        List<NotificationEntity> listOfNotifications = new ArrayList<>();
        NotificationEntity notification = createNotificationsForUser(userEntity);
        listOfNotifications.add(notification);
        return listOfNotifications;
    }

    private NotificationEntity createNotificationsForUser(UserEntity userEntity) {
        NotificationEntity notification = new NotificationEntity();
//        notification.setBookEntity(!userEntity.getRentedBooks().isEmpty() ? userEntity.getRentedBooks().get(0) : createBookForUser(userEntity));
        notification.setUserEntity(userEntity);
        notification.setAlreadyRead(false);
        notification.setReadingTimeByUser(DateUtils.parseDateToLocalDateTime(new Date()));
        notification.setMessage(NotificationMessages.USER_HAS_RENTED_BOOK);
        return notification;
    }
}
