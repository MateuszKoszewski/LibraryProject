package com.mateusz.library.model.dao;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String name;

    private String lastName;

    private String email;

//    @OneToMany(
//            cascade = CascadeType.ALL,
//            mappedBy = "users")
    private List<BookEntity> rentedBooks;

}
