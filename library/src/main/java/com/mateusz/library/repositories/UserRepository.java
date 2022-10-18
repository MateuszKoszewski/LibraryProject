package com.mateusz.library.repositories;

import com.mateusz.library.model.dao.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

UserEntity findByEmail (String email);
UserEntity findUserByUsername(String login);
}
