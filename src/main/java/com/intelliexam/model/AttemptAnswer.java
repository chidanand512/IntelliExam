package com.intelliexam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Attempt attempt;

    @ManyToOne(optional = false)
    private Question question;

    private String selectedOption;

    @Column(nullable = false)
    private boolean correct;
}
