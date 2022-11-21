package com.mateusz.library.services;

import com.mateusz.library.constants.NotificationMessages;
import com.mateusz.library.model.dao.NotificationEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.utils.AuthenticationUtils;
import com.mateusz.library.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;

    public List<NotificationEntity> getLoggedInUserAllNotifications() {
        List<NotificationEntity> listOfUserNotifications = getLoggedInUserNotifications();
        List<NotificationEntity> duplicatedListOfUserNotifications = duplicateListOfNotifications(listOfUserNotifications);
        setNotificationsAlreadyReadToTrue(listOfUserNotifications);
        setReadingTimeOfNotifications(listOfUserNotifications);
        return duplicatedListOfUserNotifications;
    }

    public List<NotificationEntity> getLoggedInUserCurrentNotifications() {
        List<NotificationEntity> allNotifications = getLoggedInUserAllNotifications();
        return allNotifications.stream().filter(notification -> !notification.isAlreadyRead()).toList();
    }

    public List<NotificationEntity> deleteNotifications() {
        UserEntity loggedInUser = getLoggedInUserEntity();
        List<NotificationEntity> listOfUserNotifications = loggedInUser.getNotifications();
        List<NotificationEntity> listWithNotificationsToRemove = new ArrayList<>();

        for (NotificationEntity notification : listOfUserNotifications) {
            if (notification.isAlreadyRead()) {
                listWithNotificationsToRemove.add(notification);
            }
        }
        loggedInUser.removeParticularNotifications(listWithNotificationsToRemove);
        return listWithNotificationsToRemove;
    }

    private List<NotificationEntity> getLoggedInUserNotifications() {
        UserEntity loggedInUser = getLoggedInUserEntity();
        return loggedInUser.getNotifications();
    }

    private UserEntity getLoggedInUserEntity() {
        String currentlyLoggedInUsername = AuthenticationUtils.getCurrentlyLoggedInUsername();
        return userRepository.findUserByUsername(currentlyLoggedInUsername);
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

    private void setNotificationsAlreadyReadToTrue(List<NotificationEntity> listOfUserNotifications) {
        for(NotificationEntity notification: listOfUserNotifications){
            notification.setAlreadyRead(true);
        }
    }

    private void setReadingTimeOfNotifications(List<NotificationEntity> listOfUserNotifications) {
        listOfUserNotifications.stream()
                .filter(notification -> notification.getReadingTimeByUser()==null)
//                .forEach(notification -> notification.setReadingTimeByUser(new Timestamp((new Date()).getTime())));
                .forEach(notification -> notification.setReadingTimeByUser(DateUtils.parseDateToLocalDateTime(new Date())));
    }

}
