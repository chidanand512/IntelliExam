package com.intelliexam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String college;
    private String branch;
    private String academicYear;
    private String registerNumber;

    @Column(nullable = false)
    private boolean publicStudent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
