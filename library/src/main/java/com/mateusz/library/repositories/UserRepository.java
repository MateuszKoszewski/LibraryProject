package com.mateusz.library.repositories;

import com.mateusz.library.constants.Role;
import com.mateusz.library.model.dao.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

UserEntity findUserByEmail(String email);
UserEntity findUserByUsername(String username);

UserEntity findUserByRoles_roleEnum(Role role);
}
