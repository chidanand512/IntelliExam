package com.intelliexam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Exam exam;

    @ManyToOne(optional = false)
    private User student;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    private Integer score;
    private Integer totalQuestions;

    @Column(nullable = false)
    private boolean submitted;
}
