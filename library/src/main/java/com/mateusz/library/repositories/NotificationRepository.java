package com.mateusz.library.repositories;

import com.mateusz.library.model.dao.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

}
