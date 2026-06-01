package com.intelliexam.repository;

import com.intelliexam.model.Exam;
import com.intelliexam.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExam(Exam exam);
}
