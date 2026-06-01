package com.intelliexam.repository;

import com.intelliexam.model.Attempt;
import com.intelliexam.model.Exam;
import com.intelliexam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByExam(Exam exam);
    List<Attempt> findByStudent(User student);
    Optional<Attempt> findByExamAndStudent(Exam exam, User student);
    Optional<Attempt> findByExamAndStudentAndSubmittedFalse(Exam exam, User student);
}
