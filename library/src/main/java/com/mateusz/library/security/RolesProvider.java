package com.mateusz.library.security;

import com.mateusz.library.constants.Role;
import com.mateusz.library.constants.SuperAdminRoleConstants;
import com.mateusz.library.model.dao.RoleEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.repositories.RolesRepository;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.utils.PasswordEncoder;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class RolesProvider implements CommandLineRunner {

    private final UserRepository userRepository;

    private final RolesRepository rolesRepository;


    private final String adminPassword;

    @Autowired
    public RolesProvider(UserRepository userRepository, RolesRepository rolesRepository, @Value("${admin.password}")String adminPassword) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {

        String adminUsername = SuperAdminRoleConstants.ADMIN_USERNAME;
        String adminEmail = SuperAdminRoleConstants.ADMIN_EMAIL;
        String adminPassword = getAdminPassword();
        UserEntity admin = createBasicSuperAdmin(adminUsername, adminEmail, adminPassword);
        saveAllRolesToDB();
    }

    private void saveAllRolesToDB() {
        List<Role> allRoles = Arrays.asList(Role.values());
        for (Role currentRole: allRoles){
            if (rolesRepository.findRoleEntityByRoleEnum(currentRole) == null) {
                RoleEntity roleEntity = new RoleEntity();
                roleEntity.setRoleEnum(currentRole);
                rolesRepository.save(roleEntity);
            }
        }
    }

    private UserEntity createBasicSuperAdmin(String username, String email, String password) {
        UserEntity userEntity = userRepository.findUserByRoles_roleEnum(Role.ROLE_SUPER_ADMIN);
        if(userEntity == null) {
            UserEntity adminEntity = new UserEntity();
            adminEntity.setUsername(username);
            adminEntity.setEmail(email);
            adminEntity.setRoles(getDefaultAdminRoles());
            adminEntity.setPassword(PasswordEncoder.encodePassword(password));
            adminEntity.setActive(true);
            adminEntity.setNotLocked(true);
            adminEntity.setJoinDate(new Date());
            adminEntity.setAuthorities(Role.ROLE_SUPER_ADMIN.getAuthorities());
            adminEntity.addNewRoleEnumToTheUser(Role.ROLE_SUPER_ADMIN);
            userRepository.save(adminEntity);
            userEntity=adminEntity;
        }
        return userEntity;
    }

    private List<RoleEntity> getDefaultAdminRoles() {
        List<RoleEntity> roleEntities = new ArrayList<>();
        RoleEntity adminRoleEntity = rolesRepository.findRoleEntityByRoleEnum(Role.ROLE_SUPER_ADMIN);
        roleEntities.add(adminRoleEntity);
        return roleEntities;
    }

    public String getAdminPassword() {
        return this.adminPassword;
    }

}
