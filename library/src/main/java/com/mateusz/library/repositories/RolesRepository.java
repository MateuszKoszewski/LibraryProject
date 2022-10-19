package com.mateusz.library.repositories;

import com.mateusz.library.constants.Role;
import com.mateusz.library.model.dao.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findAll();
    RoleEntity findRoleEntityByRoleEnum(Role role);
}
