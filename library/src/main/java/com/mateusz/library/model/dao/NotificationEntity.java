package com.mateusz.library.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookEntity bookEntity;

    private String message;

    private boolean alreadyRead;

    private LocalDateTime creationTime;

    private LocalDateTime readingTimeByUser;
}
