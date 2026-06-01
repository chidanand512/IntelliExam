package com.intelliexam.repository;

import com.intelliexam.model.Exam;
import com.intelliexam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByExaminer(User examiner);
    List<Exam> findByPublishedTrue();
    Optional<Exam> findByPublicToken(String publicToken);
}
