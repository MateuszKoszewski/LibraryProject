package com.mateusz.library.model.dao;

import com.mateusz.library.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String firstName;

    private String lastName;

    private String email;

    private String username;

    private String password;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "user",
            orphanRemoval = true)
    private List<BookEntity> rentedBooks;

    private boolean isNotLocked;

    private boolean isActive;

    private Date lastLoginDate;

    private Date lastLoginDateDisplay;

    private Date joinDate;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn (name = "user_id"),
            inverseJoinColumns = @JoinColumn (name = "role_id")
    )
    private List<RoleEntity> roles;

    private String[] authorities;

    public void addRoleEntity(RoleEntity role){
        this.roles.add(role);
        role.getUserEntityList().add(this);
    }

    public void addNewRoleEnumToTheUser(Role role) {
        RoleEntity newRoleEntity = new RoleEntity();
        newRoleEntity.setRoleEnum(role);
        this.roles.add(newRoleEntity);
        newRoleEntity.getUserEntityList().add(this);
    }

    public void removeRoleEntity(RoleEntity role){
        this.roles.remove(role);
        role.getUserEntityList().remove(this);
    }

//    public void removeRoleEnumFromTheUser(Role role) {
//        RoleEntity roleEntityToRemove = this.roles.stream().filter(roleEntity -> roleEntity.getRoleEnum().equals(role)).findFirst().orElse(null);
//        this.roles.remove(roleEntityToRemove);
//
//    }

}
