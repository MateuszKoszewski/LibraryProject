package com.mateusz.library.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mateusz.library.constants.Role;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Email
    private String email;

    @NotNull
    @Size(min=2, message = "username must have at least 2 characters")
    private String username;

    @NotNull(message = "password cannot be empty")
//    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,20}$", message = "password must contain at least one digit, one lowercase character, one uppercase character and must have 6-20 characters")
    private String password;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "currentUser",
            orphanRemoval = true)
    private List<BookEntity> rentedBooks;

//    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
//    private List<HistoryOfBookEntity> historyOfBooks;

    private BigDecimal accountBalance;

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
