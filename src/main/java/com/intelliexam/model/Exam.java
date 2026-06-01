package com.intelliexam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer durationMinutes;
    private LocalDate examDate;
    private LocalTime examTime;

    @Column(nullable = false)
    private Integer totalQuestions = 10;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean resultsPublished;

    @Column(nullable = false)
    private boolean publicExam;

    @Column(nullable = false)
    private boolean negativeMarking;

    private Integer negativeMarksPerWrong = 1;

    @Column(nullable = false)
    private boolean shuffleQuestions;

    @Column(nullable = false)
    private boolean strictFullscreen = true;

    @Column(unique = true)
    private String publicToken;

    @Column(nullable = false)
    private String examPassword;

    @ManyToOne(optional = false)
    private User examiner;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
}
