package org.example;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // unique auto-generated ID

    private String action;

    private LocalDateTime timestamp;

    @ManyToOne
    private User user;

    @Column(columnDefinition = "TEXT")
    private String beforeImage;

    @Column(columnDefinition = "TEXT")
    private String afterImage;
}
