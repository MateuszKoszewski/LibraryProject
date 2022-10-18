package com.mateusz.library.model.dao;

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

    private String name;

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

    private String[] roles;

    private String[] authorities;

}
